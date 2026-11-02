package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEscapeToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "600")
public class StormcageContainmentFacility extends Card {

    public StormcageContainmentFacility() {
        addStaticEffect(new GrantEscapeToGraveyardCardsEffect(new CardTypePredicate(CardType.CREATURE)));
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.chaosEnsues(), new LockTargetPermanentEffect(
                        true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
