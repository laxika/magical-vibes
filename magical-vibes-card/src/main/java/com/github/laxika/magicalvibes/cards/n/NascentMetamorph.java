package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTargetPlayerLibraryUntilCreatureAndBecomeCopyUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "C20", collectorNumber = "36")
public class NascentMetamorph extends Card {

    public NascentMetamorph() {
        var opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        var revealAndCopy = new RevealTargetPlayerLibraryUntilCreatureAndBecomeCopyUntilEndOfTurnEffect();
        target(opponent).addEffect(EffectSlot.ON_ATTACK, revealAndCopy);
        addEffect(EffectSlot.ON_BLOCK, revealAndCopy);
    }
}
