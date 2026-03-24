package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "201")
public class RangingRaptors extends Card {

    public RangingRaptors() {
        // Enrage — Whenever this creature is dealt damage, you may search your library for a basic land card,
        // put it onto the battlefield tapped, then shuffle.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new MayEffect(
                new SearchLibraryForCardTypesToBattlefieldEffect(
                        new CardAllOfPredicate(List.of(new CardSupertypePredicate(CardSupertype.BASIC), new CardTypePredicate(CardType.LAND))),
                        true),
                "Search your library for a basic land card, put it onto the battlefield tapped?"));
    }
}
