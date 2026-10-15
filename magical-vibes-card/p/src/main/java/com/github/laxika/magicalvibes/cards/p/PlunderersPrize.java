package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "8")
public class PlunderersPrize extends Card {

    private static final CardAllOfPredicate NONLAND_ARTIFACT = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.ARTIFACT),
            new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

    public PlunderersPrize() {
        addEffect(EffectSlot.SPELL,
                new SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect(
                        NONLAND_ARTIFACT, 1));
    }
}
