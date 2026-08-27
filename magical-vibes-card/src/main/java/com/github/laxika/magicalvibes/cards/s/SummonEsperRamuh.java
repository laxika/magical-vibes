package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "161")
@CardRegistration(set = "FIN", collectorNumber = "367")
public class SummonEsperRamuh extends Card {

    private static final PermanentPredicate OPPONENT_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
    ));

    private static final CardAllOfPredicate NONCREATURE_NONLAND_CARD = new CardAllOfPredicate(List.of(
            new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
            new CardNotPredicate(new CardTypePredicate(CardType.LAND))
    ));

    public SummonEsperRamuh() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToTargetCreatureEffect(
                new CardsInGraveyard(NONCREATURE_NONLAND_CARD, CountScope.CONTROLLER),
                false,
                OPPONENT_CREATURE
        ));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(
                new PermanentPredicateTargetFilter(OPPONENT_CREATURE,
                        "Must target a creature an opponent controls")
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new BoostAllOwnCreaturesEffect(1, 0, new PermanentHasSubtypePredicate(CardSubtype.WIZARD)));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new BoostAllOwnCreaturesEffect(1, 0, new PermanentHasSubtypePredicate(CardSubtype.WIZARD)));
    }
}
