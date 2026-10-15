package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "163")
public class SoulOfShandalar extends Card {

    private static final String DAMAGE_TEXT = "3 damage to target player or planeswalker and 3 damage to "
            + "up to one target creature that player or that planeswalker's controller controls.";

    public SoulOfShandalar() {
        // {3}{R}{R}: This creature deals 3 damage to target player or planeswalker and 3 damage to
        // up to one target creature that player or that planeswalker's controller controls.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(damageEffect()),
                "{3}{R}{R}: Soul of Shandalar deals " + DAMAGE_TEXT,
                damageTargetFilters(), 1, 2
        ).withMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET));

        // {3}{R}{R}, Exile this card from your graveyard: It deals 3 damage to target player or
        // planeswalker and 3 damage to up to one target creature that player or that planeswalker's
        // controller controls.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(new ExileSelfFromGraveyardCost(), damageEffect()),
                "{3}{R}{R}, Exile this card from your graveyard: Soul of Shandalar deals " + DAMAGE_TEXT,
                damageTargetFilters(), 1, 2
        ).withMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET));
    }

    private static CardEffect damageEffect() {
        return new DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect(3, 3, 1, false, false);
    }

    /**
     * Position 0 is the player or planeswalker; position 1 the optional creature, tied to position
     * 0's controller by the cross-target constraint.
     */
    private static List<TargetFilter> damageTargetFilters() {
        return List.of(
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player or planeswalker"),
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"));
    }
}
