package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "STA", collectorNumber = "48")
@CardRegistration(set = "CMM", collectorNumber = "269")
@CardRegistration(set = "MH2", collectorNumber = "147")
public class AbundantHarvest extends Card {

    public AbundantHarvest() {
        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        addSpellEffect(new ChooseOneAtResolutionEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Land",
                        new RevealUntilCardPredicateRestOnBottomRandomEffect(land, LibrarySearchDestination.HAND)),
                new ChooseOneEffect.ChooseOneOption("Nonland",
                        new RevealUntilCardPredicateRestOnBottomRandomEffect(new CardNotPredicate(land),
                                LibrarySearchDestination.HAND))))));
    }
}
