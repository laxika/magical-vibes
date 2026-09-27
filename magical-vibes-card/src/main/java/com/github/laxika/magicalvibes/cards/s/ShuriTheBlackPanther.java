package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MSC", collectorNumber = "95")
@CardRegistration(set = "MSC", collectorNumber = "416")
public class ShuriTheBlackPanther extends Card {

    public ShuriTheBlackPanther() {
        // Draw a card if you control three or more artifacts. Then, if you control six or more
        // artifacts, creatures you control get +2/+2 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                ConditionalEffect.unless(
                        new ControlsPermanentCount(3, new PermanentIsArtifactPredicate()),
                        new DrawCardEffect()),
                ConditionalEffect.unless(
                        new ControlsPermanentCount(6, new PermanentIsArtifactPredicate()),
                        new BoostAllOwnCreaturesEffect(2, 2))));
    }
}
