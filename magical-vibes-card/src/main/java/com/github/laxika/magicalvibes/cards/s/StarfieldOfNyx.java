package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateControlledEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "33")
public class StarfieldOfNyx extends Card {

    public StarfieldOfNyx() {
        // "At the beginning of your upkeep, you may return target enchantment card from your
        // graveyard to the battlefield." The target is chosen as the trigger goes on the stack
        // (CR 603.3d); the "you may" is the up-to-one graveyard pick, which can be left empty.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                .targetGraveyard(true)
                .upTo(true)
                .build());

        addEffect(EffectSlot.STATIC, new AnimateControlledEnchantmentsEffect(5));
    }
}
