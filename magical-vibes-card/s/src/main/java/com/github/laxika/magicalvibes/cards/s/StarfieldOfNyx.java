package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AnimateControlledEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "33")
@CardRegistration(set = "PIO", collectorNumber = "36")
public class StarfieldOfNyx extends Card {

    public StarfieldOfNyx() {
        // "At the beginning of your upkeep, you may return target enchantment card from your
        // graveyard to the battlefield." The target is chosen as the trigger goes on the stack
        // (CR 603.3d), and is required; the "you may" is decided on resolution (CR 603.5), so the
        // return can be declined after the target was chosen. An Aura returned this way chooses
        // what it enchants as it enters.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                .targetGraveyard(true)
                .chooseAuraAttachment(true)
                .build(), "Return the targeted enchantment card to the battlefield?"));

        addEffect(EffectSlot.STATIC, new AnimateControlledEnchantmentsEffect(5));
    }
}
