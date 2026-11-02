package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "73")
@CardRegistration(set = "TMT", collectorNumber = "268")
@CardRegistration(set = "TMT", collectorNumber = "320")
public class SharkShredderKillerClone extends Card {

    public SharkShredderKillerClone() {
        addSneak("{3}{B}{B}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .source(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                        .targetGraveyard(true)
                        .upTo(true)
                        .enterTapped(true)
                        .enterAttacking(true)
                        .build());
    }
}
