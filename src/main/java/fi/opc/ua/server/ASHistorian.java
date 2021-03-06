/**
 * Prosys OPC UA Java SDK
 *
 * Copyright (c) 2009-2010 Prosys PMS Ltd., <http://www.prosysopc.com>. 
 * All rights reserved.
 */
package fi.opc.ua.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.AccessLevel;
import org.opcfoundation.ua.core.AggregateConfiguration;
import org.opcfoundation.ua.core.EventFilter;
import org.opcfoundation.ua.core.HistoryData;
import org.opcfoundation.ua.core.HistoryEvent;
import org.opcfoundation.ua.core.HistoryEventFieldList;
import org.opcfoundation.ua.core.HistoryModifiedData;
import org.opcfoundation.ua.core.HistoryReadDetails;
import org.opcfoundation.ua.core.HistoryReadValueId;
import org.opcfoundation.ua.core.HistoryUpdateDetails;
import org.opcfoundation.ua.core.HistoryUpdateResult;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.PerformUpdateType;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.utils.NumericRange;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.encoding.DecodingException;

import com.prosysopc.ua.EventNotifierClass;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.ServerConnectionException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReference;
import com.prosysopc.ua.server.HistoryContinuationPoint;
import com.prosysopc.ua.server.HistoryManagerListener;
import com.prosysopc.ua.server.HistoryResult;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.server.nodes.UaObjectNode;
import com.prosysopc.ua.server.nodes.UaVariableNode;


/**
 * A sample implementation of a data historian.
 * <p>
 * It is implemented as a HistoryManagerListener. It could as well be a
 * HistoryManager, instead.
 */
public class ASHistorian implements HistoryManagerListener {
    private static Logger logger = Logger.getLogger(ASHistorian.class);
    private final Map<UaObjectNode, EventHistory> eventHistories = new HashMap<UaObjectNode, EventHistory>();
    //private HashMap<String, TargetServer> targetServers;
    private HashMap<NodeId, TargetServer> originServers = new HashMap<NodeId, TargetServer>();
    private List<TargetServer> clientList = new ArrayList<TargetServer>();
    // The variable histories
    //private final Map<UaVariableNode, ValueHistory> variableHistories = new HashMap<UaVariableNode, ValueHistory>();

    public ASHistorian() {
        super();
    }

    public void addTargetServer(TargetServer newServer) {
        clientList.add(newServer);
    }



    /**
     * Add the object to the historian for event history.
     * <p>
     * The historian will mark it to contain history (in EventNotifier
     * attribute) and it will start monitoring events for it.
     * 
     * @param node
     *            the object to initialize
     */
    public void addEventHistory(UaObjectNode node) {
        EventHistory history = new EventHistory(node);
        // History can be read
        EnumSet<EventNotifierClass> eventNotifier = node.getEventNotifier();
        eventNotifier.add(EventNotifierClass.HistoryRead);
        node.setEventNotifier(eventNotifier);

        eventHistories.put(node, history);
    }

    /**
     * Add the variable to the historian.
     * <p>
     * The historian will mark it to be historized and it will start monitoring
     * value changes for it.
     * 
     * @param variable
     *            the variable to initialize
     */
    public void addVariableHistory(UaVariableNode variable) {
        //ValueHistory history = new ValueHistory(variable);
        // History is being collected
        variable.setHistorizing(true);
        // History can be read
        final EnumSet<AccessLevel> READ_WRITE_HISTORYREAD = EnumSet.of(
                AccessLevel.CurrentRead, AccessLevel.CurrentWrite,
                AccessLevel.HistoryRead);
        variable.setAccessLevel(READ_WRITE_HISTORYREAD);

        //variableHistories.put(variable, history);
    }

    @Override
    public Object onBeginHistoryRead(ServiceContext serviceContext,
            HistoryReadDetails details, TimestampsToReturn timestampsToReturn,
            HistoryReadValueId[] nodesToRead,
            HistoryContinuationPoint[] continuationPoints,
            HistoryResult[] results) throws ServiceException {
        return null;
    }

    @Override
    public Object onBeginHistoryUpdate(ServiceContext serviceContext,
            HistoryUpdateDetails[] details, HistoryUpdateResult[] results,
            DiagnosticInfo[] diagnosticInfos) throws ServiceException {
        return null;
    }

    @Override
    public void onDeleteAtTimes(ServiceContext serviceContext, Object dataset,
            NodeId nodeId, UaNode node, DateTime[] reqTimes,
            StatusCode[] operationResults, DiagnosticInfo[] operationDiagnostics)
                    throws StatusException {
        /*ValueHistory history = variableHistories.get(node);
                if (history != null)
                        history.deleteAtTimes(reqTimes, operationResults,
                                        operationDiagnostics);
                else
                        throw new StatusException(StatusCodes.Bad_NoData);
         */
    }

    @Override
    public void onDeleteEvents(ServiceContext serviceContext, Object dataset,
            NodeId nodeId, UaNode node, byte[][] eventIds,
            StatusCode[] operationResults, DiagnosticInfo[] operationDiagnostics)
                    throws StatusException {
        EventHistory history = eventHistories.get(node);
        if (history != null)
            history.deleteEvents(eventIds, operationResults,
                    operationDiagnostics);
        else
            throw new StatusException(StatusCodes.Bad_NoData);
    }

    @Override
    public void onDeleteModified(ServiceContext serviceContext, Object dataset,
            NodeId nodeId, UaNode node, DateTime startTime, DateTime endTime)
                    throws StatusException {
        throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
    }

    @Override
    public void onDeleteRaw(ServiceContext serviceContext, Object dataset,
            NodeId nodeId, UaNode node, DateTime startTime, DateTime endTime)
                    throws StatusException {
        TargetServer originalServer = getOriginalServer(nodeId);
        try {
            NodeId originalId = originalServer.getNodeManager().getIdMap().get(nodeId);
            originalServer.client.client.historyDeleteRaw(originalId, startTime, endTime);
        } catch (ServerConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onEndHistoryRead(ServiceContext serviceContext, Object dataset,
            HistoryReadDetails details, TimestampsToReturn timestampsToReturn,
            HistoryReadValueId[] nodesToRead,
            HistoryContinuationPoint[] continuationPoints,
            HistoryResult[] results) throws ServiceException {
    }

    @Override
    public void onEndHistoryUpdate(ServiceContext serviceContext,
            Object dataset, HistoryUpdateDetails[] details,
            HistoryUpdateResult[] results, DiagnosticInfo[] diagnosticInfos)
                    throws ServiceException {
    }

    @Override
    public Object onReadAtTimes(ServiceContext serviceContext, Object dataset,
            TimestampsToReturn timestampsToReturn, NodeId nodeId, UaNode node,
            Object continuationPoint, DateTime[] reqTimes,
            NumericRange indexRange, HistoryData historyData)
                    throws StatusException {
        /*if (logger.isDebugEnabled())
                        logger.debug("onReadAtTimes: reqTimes=[" + reqTimes.length + "] "
                                        + ((reqTimes.length < 20) ? Arrays.toString(reqTimes) : ""));
                ValueHistory history = variableHistories.get(node);
                if (history != null)
                        historyData.setDataValues(history.readAtTimes(reqTimes));
                else
                        throw new StatusException(StatusCodes.Bad_NoData);
                return null;*/
        return null;
    }

    @Override
    public Object onReadEvents(ServiceContext serviceContext, Object dataset,
            NodeId nodeId, UaNode node, Object continuationPoint,
            DateTime startTime, DateTime endTime,
            UnsignedInteger numValuesPerNode, EventFilter filter,
            HistoryEvent historyEvent) throws StatusException {
        EventHistory history = eventHistories.get(node);
        if (history != null) {
            List<HistoryEventFieldList> events = new ArrayList<HistoryEventFieldList>();
            int firstIndex = continuationPoint == null ? 0
                    : (Integer) continuationPoint;
            Integer newContinuationPoint = history.readEvents(startTime,
                    endTime, numValuesPerNode.intValue(), filter, events,
                    firstIndex);
            historyEvent.setEvents(events
                    .toArray(new HistoryEventFieldList[events.size()]));
            return newContinuationPoint;
        } else
            throw new StatusException(StatusCodes.Bad_NoData);
    }

    @Override
    public Object onReadModified(ServiceContext serviceContext, Object dataset,
            TimestampsToReturn timestampsToReturn, NodeId nodeId, UaNode node,
            Object continuationPoint, DateTime startTime, DateTime endTime,
            UnsignedInteger numValuesPerNode, NumericRange indexRange,
            HistoryModifiedData historyData) throws StatusException {
        throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
    }

    @Override
    public Object onReadProcessed(ServiceContext serviceContext,
            Object dataset, TimestampsToReturn timestampsToReturn,
            NodeId nodeId, UaNode node, Object continuationPoint,
            DateTime startTime, DateTime endTime, Double resampleInterval,
            NodeId aggregateType,
            AggregateConfiguration aggregateConfiguration,
            NumericRange indexRange, HistoryData historyData)
                    throws StatusException {
        throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
    }

    
    public TargetServer getOriginalServer(NodeId nodeId) throws StatusException{
        /*
         * This method is used to retrieve the Server the mapped node is originally from.
         * If the node's nodeId is in the originServers structure, the corresponding server is returned.
         * If not, the method goes through the clientList, asking each of them if they have the node.
         * When the server that has that node is found, that server is saved as originalserver.
         * If, after these two ways of checking, the  originalserver is still not found, a StatusException is thrown.
         * 
         */
        TargetServer originalServer = null;
        if(originServers.containsKey(nodeId)){
            originalServer = originServers.get(nodeId);
        } else
        {
            for( int i = 0; i < clientList.size(); i++ )
            {
                TargetServer temp = clientList.get(i);
                if(temp.getNodeManager().hasNode(nodeId))
                {
                    originServers.put(nodeId, temp);
                    originalServer = temp;
                    break;
                }
            }
        }
        if (originalServer == null)
        {
            throw new StatusException("Original server for node not found");
        }
        return originalServer;
    }

    @Override
    public Object onReadRaw(ServiceContext serviceContext, Object dataset,
            TimestampsToReturn timestampsToReturn, NodeId nodeId, UaNode node,
            Object continuationPoint, DateTime startTime, DateTime endTime,
            UnsignedInteger numValuesPerNode, Boolean returnBounds,
            NumericRange indexRange, HistoryData historyData)
                    throws StatusException {  
        TargetServer originalServer = getOriginalServer(nodeId);

        
        try {
            NodeId originalId = originalServer.getNodeManager().getIdMap().get(nodeId);
            /*
            UaNode originalNode = originalServer.client.client.getAddressSpace().getNode(originalId);
            UaNode historicalConfiguration = null;
            UaReference[] originalReferences = originalNode.getReferences();

            
            for (int i = 1; i < originalReferences.length; i++)
            {
                if(originalReferences[i].getReferenceType().getBrowseName().toString().equals("HasHistoricalConfiguration"))
                {    
                    historicalConfiguration = originalReferences[i].getTargetNode();
                    System.out.println(historicalConfiguration.getProperty(new QualifiedName("Stepped")).getValue().getValue().toString());
                    break;
                }
            }
            */
            DataValue[] test = originalServer.client.client.historyReadRaw(originalId, startTime, endTime, UnsignedInteger.ONE, true, null, TimestampsToReturn.Source);
            historyData.setDataValues(test);
        } catch (ServerConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DecodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        /*} catch (AddressSpaceException problem) {
            // TODO Auto-generated catch block
            problem.printStackTrace();
        */
        }
        return null;
    }

    @Override
    public void onUpdateData(ServiceContext serviceContext, Object dataset,
            NodeId nodeId, UaNode node, DataValue[] updateValues,
            PerformUpdateType performInsertReplace,
            StatusCode[] operationResults, DiagnosticInfo[] operationDiagnostics)
                    throws StatusException {
        throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
    }

    @Override
    public void onUpdateEvent(ServiceContext serviceContext, Object dataset,
            NodeId nodeId, UaNode node, Variant[] eventFields,
            EventFilter filter, PerformUpdateType performInsertReplace,
            StatusCode[] operationResults, DiagnosticInfo[] operationDiagnostics)
                    throws StatusException {
        throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
    }

    @Override
    public void onUpdateStructureData(ServiceContext serviceContext,
            Object dataset, NodeId nodeId, UaNode node,
            DataValue[] updateValues, PerformUpdateType performUpdateType,
            StatusCode[] operationResults, DiagnosticInfo[] operationDiagnostics)
                    throws StatusException {
        throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
    }

};