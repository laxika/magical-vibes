package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
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
                        SequenceEffect.of(
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                                new DrawCardEffect(2))
                ).withManaCost("{2}{R}").withSpellType(CardType.INSTANT),
                new ChooseOneEffect.ChooseOneOption(
                        "Furious - Deals 3 damage to each creature without flying",
                        new MassDamageEffect(3, false, false,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))
                ).withManaCost("{3}{R}{R}").withSpellType(CardType.SORCERY)
        )));
    }
}
