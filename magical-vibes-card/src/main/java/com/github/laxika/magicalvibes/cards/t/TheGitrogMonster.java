package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "INR", collectorNumber = "238")
@CardRegistration(set = "INR", collectorNumber = "431")
public class TheGitrogMonster extends Card {

    public TheGitrogMonster() {
        // At the beginning of your upkeep, sacrifice The Gitrog Monster unless you sacrifice a land.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new SacrificeUnlessSacrificeOwnPermanentEffect(new PermanentIsLandPredicate(), "a land"));

        // You may play an additional land on each of your turns.
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));

        // Whenever one or more land cards are put into your graveyard from anywhere, draw a card.
        addEffect(EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, new DrawCardEffect(1));
    }
}
