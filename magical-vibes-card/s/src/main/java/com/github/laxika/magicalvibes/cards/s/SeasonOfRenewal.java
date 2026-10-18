package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "205")
public class SeasonOfRenewal extends Card {

    public SeasonOfRenewal() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(
                                new CardTypePredicate(CardType.CREATURE))),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target enchantment card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(
                                new CardTypePredicate(CardType.ENCHANTMENT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card and target enchantment card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.oneOfEachCardType(
                                CardType.CREATURE, CardType.ENCHANTMENT))
        )));
    }
}
