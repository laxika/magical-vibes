package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;

import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "95")
public class ViridianEmissary extends Card {

    public ViridianEmissary() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new SearchLibraryForCardTypesToBattlefieldEffect(Set.of(CardType.LAND), true, true),
                "Search your library for a basic land card, put it onto the battlefield tapped?"));
    }
}
