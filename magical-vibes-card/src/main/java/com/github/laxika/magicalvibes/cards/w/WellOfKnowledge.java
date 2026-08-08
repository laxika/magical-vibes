package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "162")
public class WellOfKnowledge extends Card {

    public WellOfKnowledge() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new DrawCardEffect()),
                "{2}: Draw a card. Any player may activate this ability but only during their draw step.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_DRAW_STEP
        ).withActivatableByAnyPlayer());
    }
}
