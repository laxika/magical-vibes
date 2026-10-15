package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "112")
public class MarchOfTheDrowned extends Card {

    public MarchOfTheDrowned() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardTypePredicate(CardType.CREATURE), 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return two target Pirate cards from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardSubtypePredicate(CardSubtype.PIRATE), 2))
        )));
    }
}
