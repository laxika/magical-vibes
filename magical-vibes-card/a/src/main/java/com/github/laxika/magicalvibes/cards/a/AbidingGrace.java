package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "1")
public class AbidingGrace extends Card {

    public AbidingGrace() {
        var oneManaCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMinManaValuePredicate(1),
                new CardMaxManaValuePredicate(1)));
        var ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "You gain 1 life",
                        new GainLifeEffect(1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card with mana value 1 from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(oneManaCreature)
                                .source(ownGraveyard)
                                .targetGraveyard(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(oneManaCreature, ownGraveyard))
        )));
    }
}
