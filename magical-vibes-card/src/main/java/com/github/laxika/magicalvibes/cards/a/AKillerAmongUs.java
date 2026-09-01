package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSourceChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "167")
public class AKillerAmongUs extends Card {

    private static final List<CardSubtype> CHOOSABLE_TYPES = List.of(
            CardSubtype.HUMAN, CardSubtype.MERFOLK, CardSubtype.GOBLIN);

    private static final PermanentAllOfPredicate ATTACKING_CREATURE_TOKEN = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsTokenPredicate(),
            new PermanentIsAttackingPredicate()));

    public AKillerAmongUs() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Human", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN), Set.of(), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Merfolk", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.MERFOLK), Set.of(), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Goblin", 1, 1, CardColor.RED,
                        List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseSubtypeForSourceEffect(CHOOSABLE_TYPES));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new RevealSourceChosenSubtypeEffect(),
                        PutCounterOnTargetPermanentEffect.withResolutionCondition(
                                CounterType.PLUS_ONE_PLUS_ONE, 3,
                                new PermanentHasSourceChosenSubtypePredicate()),
                        GrantKeywordEffect.toTargetIf(Keyword.DEATHTOUCH,
                                new PermanentHasSourceChosenSubtypePredicate())),
                "Sacrifice this enchantment, Reveal the creature type you chose: If target attacking creature token is the chosen type, put three +1/+1 counters on it and it gains deathtouch until end of turn.",
                new PermanentPredicateTargetFilter(ATTACKING_CREATURE_TOKEN,
                        "Target must be an attacking creature token")));
    }
}
