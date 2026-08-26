package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostsOfTheInnocent.class, Blaze.class, GrizzlyBears.class, SerraAngel.class})
class GhostsOfTheInnocentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals half damage to its controller, rounded down")
    void halvesDamageToController() {
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 5, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals half damage to a permanent its controller controls, rounded down")
    void halvesDamageToControlledPermanent() {
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        Permanent serraAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 8);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 5, serraAngel.getId());
        harness.passBothPriorities();

        assertThat(serraAngel.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Deals half combat damage to its controller, rounded down")
    void halvesCombatDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GhostsOfTheInnocent());
        GrizzlyBears attacker = new GrizzlyBears();
        attacker.setPower(5);
        attacker.setToughness(5);
        addCreatureReady(player2, attacker);

        declareAttackers(player2, List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}
