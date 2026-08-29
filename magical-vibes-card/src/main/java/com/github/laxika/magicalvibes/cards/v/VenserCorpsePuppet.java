package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "219")
public class VenserCorpsePuppet extends Card {

    private static final String HOLLOW_SENTINEL_NAME = "The Hollow Sentinel";
    private static final PermanentPredicate HOLLOW_SENTINEL = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNamedPredicate(HOLLOW_SENTINEL_NAME)));
    private static final PermanentPredicate ARTIFACT_CREATURE_YOU_CONTROL = new PermanentAllOfPredicate(List.of(
            new PermanentControlledBySourceControllerPredicate(),
            new PermanentIsArtifactPredicate(),
            new PermanentIsCreaturePredicate()));

    public VenserCorpsePuppet() {
        CreateTokenEffect hollowSentinel = new CreateTokenEffect(
                CardType.CREATURE, 1, HOLLOW_SENTINEL_NAME, 3, 3, null, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT),
                false, false, Map.of(), List.of(), false, false, true, 0, Set.of());

        addEffect(EffectSlot.ON_CONTROLLER_PROLIFERATES, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "If you don't control a creature named The Hollow Sentinel, create The Hollow Sentinel",
                        new ConditionalEffect(new NotCondition(new ControlsPermanent(HOLLOW_SENTINEL)), hollowSentinel)),
                new ChooseOneEffect.ChooseOneOption(
                        "Target artifact creature you control gains flying and lifelink until end of turn",
                        new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.LIFELINK), GrantScope.TARGET,
                                ARTIFACT_CREATURE_YOU_CONTROL),
                        new PermanentPredicateTargetFilter(
                                ARTIFACT_CREATURE_YOU_CONTROL,
                                "Target must be an artifact creature you control"))
        )));
    }
}
