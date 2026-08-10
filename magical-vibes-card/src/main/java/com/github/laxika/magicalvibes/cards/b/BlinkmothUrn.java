package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MRD", collectorNumber = "145")
public class BlinkmothUrn extends Card {

    public BlinkmothUrn() {
        addEffect(EffectSlot.EACH_PRECOMBAT_MAIN_TRIGGERED,
                new ConditionalEffect(
                        new SourceUntapped(),
                        new AwardManaToActivePlayerEffect(
                                ManaColor.COLORLESS,
                                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER))));
    }
}
