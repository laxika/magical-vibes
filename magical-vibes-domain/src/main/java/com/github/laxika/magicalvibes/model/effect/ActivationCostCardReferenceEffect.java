package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** An effect whose card reference is supplied by a card chosen during activation-cost payment. */
public interface ActivationCostCardReferenceEffect extends CardEffect {

    CardEffect bindToCard(UUID cardId);
}
