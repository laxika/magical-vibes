package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AvatarRoku;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "145")
public class TheLegendOfRoku extends Card {

    public TheLegendOfRoku() {
        setBackFaceCard(new AvatarRoku());

        addTrigger(Triggers.chapter(1), new ExileTopCardMayPlayThisTurnEffect(3, false)
                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
        addTrigger(Triggers.chapter(2), new AwardAnyColorManaEffect(1));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "AvatarRoku";
    }
}
