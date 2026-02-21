package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "288")
public class RampantGrowth extends Card {

    public RampantGrowth() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForCardTypesToBattlefieldEffect(Set.of(CardType.LAND), true, true));
    }
}
