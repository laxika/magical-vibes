package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "DOM", collectorNumber = "56")
public class MerfolkTrickster extends Card {

    public MerfolkTrickster() {
        // Flash (keyword loaded from Scryfall)
        // When Merfolk Trickster enters the battlefield, tap target creature an opponent controls.
        // It loses all abilities until end of turn.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN));
    }
}
