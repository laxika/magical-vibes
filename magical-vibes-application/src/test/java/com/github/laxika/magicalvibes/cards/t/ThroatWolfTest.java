package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThroatWolfTest extends BaseCardTest {

    @BeforeAll
    static void registerTestOracle() {
        Card.registerOracle("ThroatWolf", new com.github.laxika.magicalvibes.model.OracleData(
                "Throat Wolf", CardType.CREATURE, java.util.Set.of(), "{1}{R}{R}", CardColor.RED,
                List.of(CardColor.RED), List.of(CardColor.RED), java.util.Set.of(),
                List.of(CardSubtype.WOLF), "", 3, 1, java.util.Set.of(), null, null, null));
    }

    @Test
    @DisplayName("Can be cast during an opponent's combat phase only")
    void canOnlyBeCastDuringOpponentsCombat() {
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

        gs.advanceStep(gd);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.onlyPermanentCanAttackThisCombatId).isEqualTo(wolf.getId());
    }

    private void addThroatWolfMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
