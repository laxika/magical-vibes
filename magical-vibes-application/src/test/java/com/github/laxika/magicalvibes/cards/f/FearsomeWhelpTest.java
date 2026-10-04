package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CanopyDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearsomeWhelp.class, CanopyDragon.class, GrizzlyBears.class})
class FearsomeWhelpTest extends BaseCardTest {

    @Test
    void perpetuallyReducesDragonCardsInHandButNotOtherCards() {
        CanopyDragon dragonInHand = new CanopyDragon();
        harness.setHand(player1, List.of(dragonInHand, new GrizzlyBears()));
        harness.addToBattlefield(player1, new FearsomeWhelp());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), dragonInHand)).isEqualTo(-1);
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), gd.playerHands.get(player1.getId()).get(1)))
                .isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        FearsomeWhelp dragon = new FearsomeWhelp();
        harness.setHand(player1, List.of(dragon));
        harness.addToBattlefield(player1, new FearsomeWhelp());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), dragon)).isZero();
    }

    @Test
    void multipleWhelpsStackReductionsWithoutReducingColoredMana() {
        FearsomeWhelp dragon = new FearsomeWhelp();
        FearsomeWhelp opponentsDragon = new FearsomeWhelp();
        harness.setHand(player1, List.of(dragon));
        harness.setHand(player2, List.of(opponentsDragon));
        harness.addToBattlefield(player1, new FearsomeWhelp());
        harness.addToBattlefield(player1, new FearsomeWhelp());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), dragon)).isEqualTo(-2);
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player2.getId(), opponentsDragon)).isZero();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void affectsCardsInHandAtResolutionEvenIfSourceHasLeft() {
        FearsomeWhelp removedFromHand = new FearsomeWhelp();
        FearsomeWhelp addedToHand = new FearsomeWhelp();
        harness.setHand(player1, List.of(removedFromHand));
        harness.addToBattlefield(player1, new FearsomeWhelp());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(addedToHand));
        harness.setGraveyard(player1, List.of(removedFromHand));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), addedToHand)).isEqualTo(-1);
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), removedFromHand)).isZero();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fearsome Whelp");
    }

    @Test
    void reductionPersistsAcrossZoneChangesButDoesNotAffectNewCards() {
        FearsomeWhelp reducedDragon = new FearsomeWhelp();
        FearsomeWhelp newDragon = new FearsomeWhelp();
        harness.setHand(player1, List.of(reducedDragon));
        harness.addToBattlefield(player1, new FearsomeWhelp());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(reducedDragon));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(reducedDragon, newDragon));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fearsome Whelp");
        assertThat(harness.getCastingCostService()
                .getCastCostModifier(gd, player1.getId(), newDragon)).isZero();
    }
}
