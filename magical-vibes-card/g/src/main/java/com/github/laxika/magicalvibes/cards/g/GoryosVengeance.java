package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "67")
@CardRegistration(set = "UMA", collectorNumber = "99")
@CardRegistration(set = "DSC", collectorNumber = "372")
public class GoryosVengeance extends Card {

    public GoryosVengeance() {
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new CardTypePredicate(CardType.CREATURE))))
                .targetGraveyard(true)
                .grantHaste(true)
                .exileAtEndStep(true)
                .build());
        addStaticEffect(new SpliceEffect(CardSubtype.ARCANE, "{2}{B}"));
    }
}
