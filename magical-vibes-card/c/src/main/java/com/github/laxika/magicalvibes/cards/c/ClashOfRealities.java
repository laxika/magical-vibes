package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Every Spirit permanent may deal 3 damage to a non-Spirit creature as it enters, and every
 * non-Spirit creature may deal 3 damage to a Spirit creature as it enters.
 */
@CardRegistration(set = "BOK", collectorNumber = "97")
public class ClashOfRealities extends Card {

    private static final PermanentPredicate SPIRIT = new PermanentHasSubtypePredicate(CardSubtype.SPIRIT);
    private static final PermanentPredicate NON_SPIRIT = new PermanentNotPredicate(SPIRIT);

    /**
     * The granted triggers' target restrictions spell out "creature" themselves: a may-ability that
     * carries an effect predicate is filtered by that predicate alone, so the {@code TargetSpec}'s
     * declared creature category is not conjoined in on that path.
     */
    private static final PermanentPredicate SPIRIT_CREATURE =
            new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), SPIRIT));
    private static final PermanentPredicate NON_SPIRIT_CREATURE =
            new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), NON_SPIRIT));

    public ClashOfRealities() {
        MayEffect spiritAbility = new MayEffect(
                new DealDamageToTargetCreatureEffect(3, NON_SPIRIT_CREATURE),
                "Have this Spirit deal 3 damage to target non-Spirit creature?");
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                spiritAbility, GrantScope.ALL_PERMANENTS, SPIRIT));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                spiritAbility, GrantScope.SELF, SPIRIT));

        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        new DealDamageToTargetCreatureEffect(3, SPIRIT_CREATURE),
                        "Have this creature deal 3 damage to target Spirit creature?"),
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                NON_SPIRIT_CREATURE));
    }
}
