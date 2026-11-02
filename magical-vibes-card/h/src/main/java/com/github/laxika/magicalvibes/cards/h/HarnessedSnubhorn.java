package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "3")
@CardRegistration(set = "MAT", collectorNumber = "53")
@CardRegistration(set = "MAT", collectorNumber = "103")
@CardRegistration(set = "MAT", collectorNumber = "188")
public class HarnessedSnubhorn extends Card {

    public HarnessedSnubhorn() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.ENCHANTMENT))))
                .targetGraveyard(true)
                .chooseAuraAttachment(true)
                .build());
    }
}
