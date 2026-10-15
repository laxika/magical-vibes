package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YEOE", collectorNumber = "17")
public class ThendarTheOverminer extends Card {

    public ThendarTheOverminer() {
        CardPredicate nonbasicLand = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.BASIC))));
        addEffect(EffectSlot.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        nonbasicLand,
                        new ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect(
                                Wastes::new, Set.of(CardType.LAND))));

        PermanentPredicate tappedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate()));
        PermanentPredicate nonbasicLandPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))));
        target(new PermanentPredicateTargetFilter(
                        nonbasicLandPermanent, "Target must be a nonbasic land"), 0, 1)
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new ConditionalEffect(
                                new ControlsPermanentCount(2, tappedCreature),
                                new DestroyTargetPermanentEffect()));
    }
}
