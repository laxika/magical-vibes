package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "M20", collectorNumber = "295")
public class ChandrasFlameWave extends Card {

    public ChandrasFlameWave() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ));
        addSimultaneousSpellEffects(DealDamageEffect.toTargetPlayerOrPlaneswalker(2),
                DealDamageToPermanentsTargetControlsEffect.toCreaturesOfTargetedPlayer(new Fixed(2)));
        addSpellEffect(new SearchLibraryAndOrGraveyardForCardToHandEffect(
                new CardNamedPredicate("Chandra, Flame's Fury")));
    }
}
