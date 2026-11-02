package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsWithResultEffectsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "218")
public class YusriFortunesFlame extends Card {

    public YusriFortunesFlame() {
        addTrigger(Triggers.attacks(), new ChooseNumberAtResolutionEffect(1, 5));
        addTrigger(Triggers.attacks(), new FlipCoinsWithResultEffectsEffect(
                new XValue(),
                new DrawCardEffect(1),
                new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER),
                new GrantStaticEffectToPlayerUntilEndOfTurnEffect(
                        new AlternativeCostForSpellsEffect("{0}", null, null, false, true))));
    }
}
