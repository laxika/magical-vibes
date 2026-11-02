package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "30")
public class CallToTheKindred extends Card {

    public CallToTheKindred() {
        target(TargetFilters.creature())
        .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(
                        new LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect(5),
                        "Look at the top five cards of your library?"
                ));
    }
}
