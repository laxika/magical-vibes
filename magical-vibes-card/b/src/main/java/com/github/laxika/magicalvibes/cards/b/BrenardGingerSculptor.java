package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "27")
@CardRegistration(set = "WOC", collectorNumber = "35")
public class BrenardGingerSculptor extends Card {

    private static final ActivatedAbility FOOD_GOLEM_ABILITY = new ActivatedAbility(
            true,
            "{2}",
            List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
            "{2}, {T}, Sacrifice this token: You gain 3 life."
    );

    public BrenardGingerSculptor() {
        addStaticEffect(new StaticBoostEffect(
                2,
                2,
                Set.of(Keyword.TRAMPLE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FOOD, CardSubtype.GOLEM))
        ));
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new MayEffect(
                new ExileDyingCreatureAndCreateTokenCopyEffect(foodGolemCopy()),
                "Exile that creature to create a Food Golem token copy?"
        ));
    }

    private static CreateTokenCopyOfTargetPermanentEffect foodGolemCopy() {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.FOOD, CardSubtype.GOLEM),
                Set.of(CardType.ARTIFACT),
                1,
                1,
                Map.of(),
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                Set.of(),
                false,
                List.of(
                        StaticAbility.of(List.of(
                        new GrantActivatedAbilityEffect(FOOD_GOLEM_ABILITY, GrantScope.SELF)
                ))),
                List.of(),
                false,
                false,
                new Fixed(1),
                false,
                Set.of(),
                false
        );
    }
}
