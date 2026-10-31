package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "242")
@CardRegistration(set = "C20", collectorNumber = "288")
@CardRegistration(set = "C19", collectorNumber = "259")
@CardRegistration(set = "FDC", collectorNumber = "306")
public class MemorialToFolly extends Card {

    public MemorialToFolly() {
        addStaticEffect(new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new SacrificeSelfCost(), ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build()),
                "{2}{B}, {T}, Sacrifice Memorial to Folly: Return target creature card from your graveyard to your hand."
        ));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
