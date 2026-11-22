package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "91")
@CardRegistration(set = "ECL", collectorNumber = "359")
@CardRegistration(set = "ECL", collectorNumber = "385")
@CardRegistration(set = "ECL", collectorNumber = "395")
public class BloodlineBidding extends Card {

    public BloodlineBidding() {
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardHasTypeChosenThisResolutionPredicate())))
                .returnAll(true)
                .chooseAuraAttachment(true)
                .build());
    }
}
