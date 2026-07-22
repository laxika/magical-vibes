package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TheMeathookMassacreTest extends BaseCardTest {

    @Test
    @DisplayName("ETB with X=2 gives all creatures -2/-2 until end of turn")
    void etbDebuffsAllCreaturesByX() {
        harness.addToBattlefield(player1, new HillGiant()); // 3/3
        harness.addToBattlefield(player2, new HillGiant()); // 3/3

        castMeathook(2);
        harness.passBothPriorities(); // resolve spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB

        Permanent own = giant(player1);
        Permanent opp = giant(player2);
        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(own.getEffectiveToughness()).isEqualTo(1);
        assertThat(opp.getEffectivePower()).isEqualTo(1);
        assertThat(opp.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB with X=2 kills 2/2 creatures via state-based actions")
    void etbKillsCreaturesWithToughnessLeX() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMeathook(2);
        harness.passBothPriorities(); // resolve spell
        harness.passBothPriorities(); // resolve ETB → -2/-2 → SBAs kill bears

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(findMeathook()).isNotNull();
    }

    @Test
    @DisplayName("Debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HillGiant());

        castMeathook(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent giant = giant(player1);
        assertThat(giant.getEffectivePower()).isEqualTo(2);
        assertThat(giant.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);

        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Whenever a creature you control dies, each opponent loses 1 life")
    void allyDeathDrainsOpponents() {
        harness.addToBattlefield(player1, new TheMeathookMassacre());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Shock
        harness.passBothPriorities(); // death trigger

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Whenever an opponent's creature dies, you gain 1 life")
    void opponentDeathGainsLife() {
        harness.addToBattlefield(player1, new TheMeathookMassacre());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Shock
        harness.passBothPriorities(); // death trigger

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Creatures dying to the ETB -X/-X fire both death abilities")
    void creaturesDyingToEtbFireDeathTriggers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castMeathook(2);
        harness.passBothPriorities(); // resolve spell → ETB on stack
        harness.passBothPriorities(); // ETB → both bears die → death triggers stack
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertLife(player2, 19); // ally death
        harness.assertLife(player1, 21); // opponent death
    }

    private void castMeathook(int x) {
        harness.setHand(player1, List.of(new TheMeathookMassacre()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, x);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.playCard(gd, player1, 0, x, null, null);
    }

    private Permanent findMeathook() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("The Meathook Massacre"))
                .findFirst()
                .orElse(null);
    }

    private Permanent giant(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst()
                .orElseThrow();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
