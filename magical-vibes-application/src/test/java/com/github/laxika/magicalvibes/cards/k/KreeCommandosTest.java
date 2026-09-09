package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KreeCommandos.class, GrizzlyBears.class, Shock.class})
class KreeCommandosTest extends BaseCardTest {

    private Permanent addCommando() {
        harness.addToBattlefield(player1, new KreeCommandos());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent commando = addCommando();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent commando = addCommando();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell does not trigger prowess")
    void opponentNoncreatureSpellDoesNotPump() {
        Permanent commando = addCommando();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(1);
    }

    @Test
    @DisplayName("The prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent commando = addCommando();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(3);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(1);
    }
}
