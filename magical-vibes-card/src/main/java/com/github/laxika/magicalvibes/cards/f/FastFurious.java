package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/** Fast // Furious, a split spell with one mode for each half. */
@CardRegistration(set = "MH2", collectorNumber = "123")
public class FastFurious extends Card {

    public FastFurious() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Fast - Discard a card, then draw two cards",
                        new DiscardAndDrawCardEffect(1, 2)
                ).withManaCost("{2}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Furious - Deals 3 damage to each creature without flying",
                        new MassDamageEffect(3, false, false,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))
                ).withManaCost("{3}{R}{R}")
        )));
    }
}
