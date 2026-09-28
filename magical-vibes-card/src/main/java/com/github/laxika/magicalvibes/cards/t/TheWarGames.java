package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentThenExileMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreatedPermanentsUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "30")
public class TheWarGames extends Card {

    private static final PermanentPredicate WARRIOR = new PermanentHasSubtypePredicate(CardSubtype.WARRIOR);
    private static final PermanentPredicate NONTOKEN_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsTokenPredicate()),
            new PermanentControlledBySourceControllerPredicate()));

    public TheWarGames() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, SequenceEffect.of(
                new EachPlayerCreatesTokenEffect(new CreateTokenEffect(
                        3, "Warrior", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.WARRIOR), Set.of(), Set.of()).withTapped(true)
                ),
                new GoadCreatedPermanentsUntilSourceLeavesEffect()));
        PutCounterOnEachMatchingPermanentEffect warriorCounter = new PutCounterOnEachMatchingPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, WARRIOR, EachPermanentScope.ALL_PLAYERS);
        addEffect(EffectSlot.SAGA_CHAPTER_II, warriorCounter);
        addEffect(EffectSlot.SAGA_CHAPTER_III, warriorCounter);
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new MayEffect(
                new ExilePermanentThenExileMatchingPermanentsEffect(
                        NONTOKEN_CREATURE, WARRIOR, "nontoken creature you control"),
                "Exile a nontoken creature you control?"));
    }
}
