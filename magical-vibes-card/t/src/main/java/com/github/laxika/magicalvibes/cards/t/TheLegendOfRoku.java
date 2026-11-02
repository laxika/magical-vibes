package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AvatarRoku;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "145")
public class TheLegendOfRoku extends Card {

    public TheLegendOfRoku() {
        setBackFaceCard(new AvatarRoku());

        addTrigger(Triggers.chapter(1), new ExileTopCardsMayPlayUntilNextTurnEffect(3));
        addTrigger(Triggers.chapter(2), new AwardAnyColorManaEffect(1));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "AvatarRoku";
    }
}
