package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseSpellAndCastCopiesEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "132")
@CardRegistration(set = "M15", collectorNumber = "134")
@CardRegistration(set = "AKR", collectorNumber = "146")
public class ChandraPyromaster extends Card {

    public ChandraPyromaster() {
        // +1: Chandra, Pyromaster deals 1 damage to target player or planeswalker and 1 damage to
        // up to one target creature that player or that planeswalker's controller controls.
        // That creature can't block this turn.
        // Position 0 is the player-or-planeswalker; position 1 the optional creature, tied to
        // position 0's controller by the cross-target constraint.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect(1, 1, 1, false, true)),
                "+1: Chandra, Pyromaster deals 1 damage to target player or planeswalker and 1 damage to up to one target creature that player or that planeswalker's controller controls. That creature can't block this turn.",
                null, +1, null, null,
                List.<TargetFilter>of(
                        new AnyTargetPredicateTargetFilter(
                                new PermanentIsPlaneswalkerPredicate(),
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player or planeswalker"),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature")),
                1, 2
        ).withMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET));

        // 0: Exile the top card of your library. You may play it this turn.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new ExileTopCardMayPlayThisTurnEffect(false)),
                "0: Exile the top card of your library. You may play it this turn."
        ));

        // −7: Exile the top ten cards of your library. Choose an instant or sorcery card exiled
        // this way and copy it three times. You may cast the copies without paying their mana costs.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new ExileTopCardsChooseSpellAndCastCopiesEffect(10, 3)),
                "−7: Exile the top ten cards of your library. Choose an instant or sorcery card exiled this way and copy it three times. You may cast the copies without paying their mana costs."
        ));
    }
}
