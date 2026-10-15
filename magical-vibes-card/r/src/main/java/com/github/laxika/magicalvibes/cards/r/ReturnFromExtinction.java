package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "104")
public class ReturnFromExtinction extends Card {

    public ReturnFromExtinction() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(creature)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return two target creature cards that share a creature type from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyTwoSharingCreatureType(creature))
        )));
    }
}
