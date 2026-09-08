package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "225")
public class RiseToGlory extends Card {

    public RiseToGlory() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        CardIsAuraPredicate aura = new CardIsAuraPredicate();
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .targetGraveyard(true)
                                .filter(creature)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(
                                creature, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target Aura card from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .targetGraveyard(true)
                                .filter(aura)
                                .chooseAuraAttachment(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(
                                aura, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
        )));
    }
}
