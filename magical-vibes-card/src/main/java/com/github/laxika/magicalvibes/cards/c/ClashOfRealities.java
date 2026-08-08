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
 * Clash of Realities grants a mirrored optional ETB damage trigger to both halves of the
 * battlefield: every Spirit may shoot a non-Spirit creature for 3 as it enters, and every
 * non-Spirit creature may shoot a Spirit for 3 as it enters. Spirit is a creature type only,
 * so {@link GrantScope#ALL_CREATURES} covers the oracle's "All Spirits".
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
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        new DealDamageToTargetCreatureEffect(3, NON_SPIRIT_CREATURE),
                        "Have this Spirit deal 3 damage to target non-Spirit creature?"),
                GrantScope.ALL_CREATURES,
                SPIRIT));

        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        new DealDamageToTargetCreatureEffect(3, SPIRIT_CREATURE),
                        "Have this creature deal 3 damage to target Spirit creature?"),
                GrantScope.ALL_CREATURES,
                NON_SPIRIT));
    }
}
