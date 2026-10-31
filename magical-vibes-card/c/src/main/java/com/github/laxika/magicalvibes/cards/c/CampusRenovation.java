package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "27")
@CardRegistration(set = "MAT", collectorNumber = "77")
@CardRegistration(set = "MAT", collectorNumber = "127")
@CardRegistration(set = "MAT", collectorNumber = "207")
public class CampusRenovation extends Card {

    public CampusRenovation() {
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.ENCHANTMENT))))
                .targetGraveyard(true)
                .upTo(true)
                .chooseAuraAttachment(true)
                .build());
        addSpellEffect(new ExileTopCardsMayPlayUntilNextTurnEffect(2));
    }
}
