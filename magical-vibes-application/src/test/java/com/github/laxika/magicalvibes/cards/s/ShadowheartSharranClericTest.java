package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowheartSharranCleric.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Plains.class, Shock.class, Swamp.class})
class ShadowheartSharranClericTest extends BaseCardTest {

    @Test
    void endStepTriggerDealsDamageToEachPlayer() {
        addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void specializeRequiresAPlayerAtThirteenOrLessLife() {
        addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 14);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("13 or less life");
    }

    @Test
    void whiteSpecializationCreatesAKnightWhenYouLoseLifeOnYourTurn() {
        Permanent shadowheart = specialize(CardColor.WHITE, new Plains(), 0);
        loseTwoLifeDuringOwnTurn();

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
        assertThat(shadowheart.getCard().getName()).isEqualTo("Shadowheart, Cleric of Order");
    }

    @Test
    void blueSpecializationDrawsWhenYouLoseLifeOnYourTurn() {
        specialize(CardColor.BLUE, new Island(), 1);
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        loseTwoLifeDuringOwnTurn();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void blackSpecializationGainsLifeFromItsEndStepDamage() {
        specialize(CardColor.BLACK, new Swamp(), 2);
        harness.setLife(player1, 13);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void redSpecializationDealsTheLostLifeAsDamageToEachOpponent() {
        specialize(CardColor.RED, new Mountain(), 3);
        loseTwoLifeDuringOwnTurn();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void greenSpecializationAddsCountersAndCannotBeBlockedBySmallCreatures() {
        Permanent shadowheart = specialize(CardColor.GREEN, new Forest(), 4);
        loseTwoLifeDuringOwnTurn();

        assertThat(shadowheart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        shadowheart.setAttacking(true);
        shadowheart.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(shadowheart);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    private Permanent specialize(CardColor color, com.github.laxika.magicalvibes.model.Card discardedCard,
                                 int abilityIndex) {
        Permanent shadowheart = addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 13);
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return shadowheart;
    }

    private void loseTwoLifeDuringOwnTurn() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(activePlayer);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
