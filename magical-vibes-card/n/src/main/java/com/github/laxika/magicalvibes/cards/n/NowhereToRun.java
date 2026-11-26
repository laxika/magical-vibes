package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreOpponentHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.PreventOpponentCreatureWardTriggersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "111")
public class NowhereToRun extends Card {

    public NowhereToRun() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), BoostEffect.target(-3, -3));
        addStaticEffect(IgnoreOpponentHexproofEffect.opponentsCreatures());
        addStaticEffect(new PreventOpponentCreatureWardTriggersEffect());
    }
}
