package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public class LandCopyOperationState {

    public Card physicalCard;
    public Card enteringCard;
    public UUID controllerId;
    public boolean landPlay;
    public Zone landPlayZone;
    public boolean initiallyTapped;
    public String logSuffix;
}
