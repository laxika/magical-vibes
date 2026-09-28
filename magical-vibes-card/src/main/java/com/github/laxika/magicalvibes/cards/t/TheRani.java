package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GoadEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsGoadedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "149")
public class TheRani extends Card {

    private static final PermanentPredicate ANOTHER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

    public TheRani() {
        target(new PermanentPredicateTargetFilter(ANOTHER_CREATURE, "Target must be another creature"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CreateTokenAttachedToTargetEffect(markOfTheRaniToken(), PlayerRelation.ANY))
                .addEffect(EffectSlot.ON_ATTACK,
                        new CreateTokenAttachedToTargetEffect(markOfTheRaniToken(), PlayerRelation.ANY));

        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsGoadedPredicate(),
                        CreateTokenEffect.ofClueToken(1)));
    }

    private static CreateTokenEffect markOfTheRaniToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Mark of the Rani",
                0,
                0,
                CardColor.RED,
                null,
                List.of(CardSubtype.AURA),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(
                        EffectSlot.STATIC, SequenceEffect.of(
                                new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE),
                                new GoadEquippedCreatureEffect())),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of())
                .withTokenTargetFilter(TargetFilters.creature());
    }
}
