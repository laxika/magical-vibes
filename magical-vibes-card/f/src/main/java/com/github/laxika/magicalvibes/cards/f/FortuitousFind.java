package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "81")
@CardRegistration(set = "KLR", collectorNumber = "86")
public class FortuitousFind extends Card {

    public FortuitousFind() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target artifact card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(new CardTypePredicate(CardType.ARTIFACT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(new CardTypePredicate(CardType.CREATURE))),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target artifact card and target creature card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.oneOfEachCardType(
                                CardType.ARTIFACT, CardType.CREATURE))
        )));
    }
}
