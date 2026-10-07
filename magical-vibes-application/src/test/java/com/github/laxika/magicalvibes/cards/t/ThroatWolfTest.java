package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThroatWolf.class, WallOfStone.class, WhiteKnight.class})
class ThroatWolfTest extends BaseCardTest {

    @BeforeAll
    static void registerTestOracle() {
        Card.registerOracle("ThroatWolf", new com.github.laxika.magicalvibes.model.OracleData(
                "Throat Wolf", CardType.CREATURE, java.util.Set.of(), "{1}{R}{R}", CardColor.RED,
                List.of(CardColor.RED), List.of(CardColor.RED), java.util.Set.of(),
                List.of(CardSubtype.WOLF), "", 3, 1, java.util.Set.of(), null, null, null));
    }

    @Test
    @DisplayName("Can be cast during an opponent's combat but not its controller's combat")
    void canBeCastDuringOpponentsCombat() {
        harness.setHand(player1, List.of(new ThroatWolf()));
        addThroatWolfMana();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Throat Wolf"));
    }

    @Test
    @DisplayName("Firstest strike does not deal combat damage to an unblocked player")
    void firstestStrikeOnlyDamagesCreatures() {
        harness.setLife(player2, 20);
        Permanent wolf = addCreatureReady(player1, new ThroatWolf());
        wolf.setAttacking(true);
        wolf.setAttackTarget(player2.getId());

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("After an opponent's first combat, creates a restricted extra combat")
    void createsRestrictedExtraCombat() {
        Permanent wolf = addCreatureReady(player1, new ThroatWolf());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        assertThat(wolf.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can be cast normally during its controller's main phase")
    void canBeCastDuringControllersMainPhase() {
        harness.setHand(player1, List.of(new ThroatWolf()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addThroatWolfMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Throat Wolf");
    }

    @Test
    @DisplayName("Firstest strike kills a first-strike blocker before it deals damage")
    void killsFirstStrikeBlockerBeforeItDealsDamage() {
        addCreatureReady(player1, new ThroatWolf());
        addCreatureReady(player2, new WhiteKnight());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Throat Wolf");
        harness.assertInGraveyard(player2, "White Knight");
    }

    @Test
    @DisplayName("Firstest strike does not deal damage again during regular combat damage")
    void dealsDamageOnlyOnceToSurvivingBlocker() {
        addCreatureReady(player1, new ThroatWolf());
        Permanent wall = addCreatureReady(player2, new WallOfStone());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Throat Wolf");
    }

    @Test
    @DisplayName("A firstest striker entering attacking after the firstest step deals regular damage")
    void lateFirstestStrikerDealsRegularDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatDamageFirstestStrikeStepComplete = true;
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new ThroatWolf());
        wolf.setAttacking(true);
        wolf.setAttackTarget(player2.getId());
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private void addThroatWolfMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
