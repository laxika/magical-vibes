package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "182")
public class KheruLichLord extends Card {

    public KheruLichLord() {
        // At the beginning of your upkeep, you may pay {2}{B}. If you do, return a creature card
        // at random from your graveyard to the battlefield. It gains flying, trample, and haste.
        // Exile that card at the beginning of your next end step, or if it would leave the battlefield.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayPayManaEffect(
                "{2}{B}",
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .returnAtRandom(true)
                        .grantKeywords(Set.of(Keyword.FLYING, Keyword.TRAMPLE, Keyword.HASTE))
                        .exileAtYourNextEndStep(true)
                        .exileIfLeavesBattlefield(true)
                        .build(),
                "Pay {2}{B} to return a creature card at random from your graveyard to the battlefield?"));
    }
}
