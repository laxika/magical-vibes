package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "164")
public class GrowFromTheAshes extends Card {

    public GrowFromTheAshes() {
        var basicLandFilter = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardTypePredicate(CardType.LAND)
        ));

        // Kicker {2}
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));

        // Search your library for a basic land card, put it onto the battlefield, then shuffle.
        // If this spell was kicked, instead search your library for two basic land cards,
        // put them onto the battlefield, then shuffle.
        addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                new SearchLibraryForCardTypesToBattlefieldEffect(basicLandFilter, false, 1),
                new SearchLibraryForCardTypesToBattlefieldEffect(basicLandFilter, false, 2)
        ));
    }
}
