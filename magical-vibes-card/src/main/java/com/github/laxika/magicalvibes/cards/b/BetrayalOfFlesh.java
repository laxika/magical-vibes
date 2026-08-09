package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateSacrificeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "58")
public class BetrayalOfFlesh extends Card {

    public BetrayalOfFlesh() {
        addEffect(EffectSlot.SPELL, new EscalateSacrificeCost(3, new PermanentIsLandPredicate()));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .targetGraveyard(true)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .build())
        )));
    }
}
