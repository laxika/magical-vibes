package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "119")
@CardRegistration(set = "SLD", collectorNumber = "512")
public class ChandraFireArtisan extends Card {

    public ChandraFireArtisan() {
        addTrigger(Triggers.loyaltyCountersRemovedFromThis(),
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(new EventValue(), PlayerRelation.OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ExileTopCardMayPlayThisTurnEffect(false)),
                "+1: Exile the top card of your library. You may play it this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new ExileTopCardMayPlayThisTurnEffect(7, false)),
                "\u22127: Exile the top seven cards of your library. You may play them this turn."
        ));
    }
}
