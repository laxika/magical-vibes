package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CloneOperationState {

    public Card card;
    public UUID controllerId;
    public UUID etbTargetId;
    public Integer powerOverride;
    public Integer toughnessOverride;
    public Set<CardType> additionalTypesOverride = Set.of();
    public List<ActivatedAbility> additionalActivatedAbilities = List.of();
}
