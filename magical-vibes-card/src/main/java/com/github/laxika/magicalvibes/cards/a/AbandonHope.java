package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "107")
public class AbandonHope extends Card {

    public AbandonHope() {
        // Additional cost: discard X cards (the same X the spell is cast for).
        addEffect(EffectSlot.SPELL, new DiscardXCardsCost());
        // Look at target opponent's hand and choose X cards from it; that player discards them.
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                new XValue(), List.of(), HandChoiceDestination.DISCARD));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));
    }
}
