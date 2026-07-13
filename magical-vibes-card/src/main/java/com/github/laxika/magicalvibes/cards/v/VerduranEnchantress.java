package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "279")
@CardRegistration(set = "6ED", collectorNumber = "264")
@CardRegistration(set = "8ED", collectorNumber = "285")
@CardRegistration(set = "7ED", collectorNumber = "280")
public class VerduranEnchantress extends Card {

    public VerduranEnchantress() {
        // Whenever you cast an enchantment spell, you may draw a card.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardTypePredicate(CardType.ENCHANTMENT),
                        List.of(new DrawCardEffect())),
                "Draw a card?"
        ));
    }
}
