package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "170")
@CardRegistration(set = "PC2", collectorNumber = "109")
@CardRegistration(set = "PCA", collectorNumber = "109")
public class FarsightMask extends Card {

    public FarsightMask() {
        addTrigger(Triggers.youAreDealtDamageBySourceOpponentControls(),
                new ConditionalEffect(
                        new SourceUntapped(),
                        new MayEffect(new DrawCardEffect(), "Draw a card?")));
    }
}
