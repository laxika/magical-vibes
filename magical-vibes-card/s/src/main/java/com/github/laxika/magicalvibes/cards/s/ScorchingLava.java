package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "INV", collectorNumber = "164")
public class ScorchingLava extends Card {

    public ScorchingLava() {
        addStaticEffect(new KickerEffect("{R}"));
        addSpellEffect(new ConditionalReplacementEffect(
                new Kicked(),
                DealDamageEffect.toAnyTarget(new Fixed(2)),
                DealDamageEffect.toAnyTarget(new Fixed(2)).cantBeRegenerated().exilingDamagedCreaturesThatWouldDie()
                        .withUnconditionalRegenerationPrevention()
        ));
    }
}
