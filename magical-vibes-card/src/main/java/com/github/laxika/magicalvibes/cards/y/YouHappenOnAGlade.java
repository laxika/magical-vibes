package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "214")
public class YouHappenOnAGlade extends Card {

    public YouHappenOnAGlade() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Journey On — Search your library for up to two basic land cards, reveal them, put them into your hand, then shuffle",
                        new SearchLibraryEffect(
                                new Fixed(2), CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND)),
                new ChooseOneEffect.ChooseOneOption(
                        "Make Camp — Return target permanent card from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardIsPermanentPredicate(), 1))
        )));
    }
}
