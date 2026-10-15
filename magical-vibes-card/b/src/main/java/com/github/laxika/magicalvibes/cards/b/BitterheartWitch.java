package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect;

@CardRegistration(set = "ISD", collectorNumber = "88")
public class BitterheartWitch extends Card {

    public BitterheartWitch() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect(CardSubtype.CURSE),
                "Search your library for a Curse card?"
        ));
    }
}
