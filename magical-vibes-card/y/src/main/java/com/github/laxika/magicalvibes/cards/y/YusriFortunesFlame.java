package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "218")
public class YusriFortunesFlame extends Card {

    public YusriFortunesFlame() {
        addTrigger(Triggers.attacks(), new ChooseNumberAtResolutionEffect(1, 5),
                FlipUntilLoseEffect.flip(
                        new XValue(),
                        new DrawCardEffect(1),
                        new DealDamageToPlayersEffect(2, Subjects.you()),
                        new ConditionalEffect(new SpellXAtLeast(5), GrantStaticEffectToPlayerEffect.untilEndOfTurn(
                                new AlternativeCostForSpellsEffect("{0}", null, null, false, true)))));
    }
}
