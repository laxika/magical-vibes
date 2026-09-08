package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "89")
public class BalefulBeholder extends Card {

    public BalefulBeholder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Antimagic Cone — Each opponent sacrifices an enchantment of their choice",
                        new SacrificePermanentsEffect(
                                1, new PermanentIsEnchantmentPredicate(), SacrificeRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Fear Ray — Creatures you control gain menace until end of turn",
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_OWN_CREATURES))
        ))));
    }
}
