package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CommittedCrimeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "OTJ", collectorNumber = "105")
public class ServantOfTheStinger extends Card {

    public ServantOfTheStinger() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ConditionalEffect(
                new CommittedCrimeThisTurn(),
                new MayEffect(
                        new SacrificeSelfThenEffect(new SearchLibraryEffect()),
                        "You may sacrifice Servant of the Stinger. If you do, search your library for a card?")));
    }
}
