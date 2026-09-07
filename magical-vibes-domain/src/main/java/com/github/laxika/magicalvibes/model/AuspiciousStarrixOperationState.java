package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AuspiciousStarrixOperationState {

    public final List<Card> permanentCards = new ArrayList<>();
    public final Deque<AuspiciousStarrixAuraChoiceRequest> pendingAuraChoices = new ArrayDeque<>();
    public final Map<UUID, UUID> auraAttachmentTargets = new HashMap<>();
    public final Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
    public AuspiciousStarrixAuraChoiceRequest activeAuraChoice;
    public UUID controllerId;

    public boolean isActive() {
        return controllerId != null;
    }

    public void clear() {
        permanentCards.clear();
        pendingAuraChoices.clear();
        auraAttachmentTargets.clear();
        enterTappedTypesSnapshot.clear();
        activeAuraChoice = null;
        controllerId = null;
    }
}
