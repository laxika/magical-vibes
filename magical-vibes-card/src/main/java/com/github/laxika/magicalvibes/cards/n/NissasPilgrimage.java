package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "190")
public class NissasPilgrimage extends Card {

    public NissasPilgrimage() {
        // Search your library for up to two basic Forest cards, reveal those cards, and put one
        // onto the battlefield tapped and the rest into your hand. Then shuffle.
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // search your library for up to three basic Forest cards instead of two.
        addEffect(EffectSlot.SPELL, new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect(
                CardSubtype.FOREST,
                new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )))));
    }
}
