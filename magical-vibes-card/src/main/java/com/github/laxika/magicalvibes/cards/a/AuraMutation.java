package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "232")
public class AuraMutation extends Card {

    public AuraMutation() {
        // Destroy target enchantment. Create X 1/1 green Saproling creature tokens, where X is
        // that enchantment's mana value.
        target(TargetFilters.enchantment()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                EventStat.MANA_VALUE,
                new CreateTokenEffect(new EventValue(), "Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of()),
                ThenEffectRecipient.CONTROLLER));
    }
}
