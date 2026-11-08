package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEqualToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMC", collectorNumber = "16")
@CardRegistration(set = "DMC", collectorNumber = "92")
public class VerrakWarpedSengir extends Card {

    public VerrakWarpedSengir() {
        // Whenever you activate an ability that isn't a mana ability, if life was paid to activate it,
        // you may pay that much life again. If you do, copy that ability. You may choose new targets for the copy.
        addTrigger(Triggers.youActivateNonManaAbility().withPayment(AbilityTrigger.ActivationPayment.LIFE),
                new MayPayLifeEqualToAmountEffect(new EventValue(), new CopyTriggeringSpellOrAbilityEffect(),
                        "Pay that much life to copy that ability?"));
    }
}
