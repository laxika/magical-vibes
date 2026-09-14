package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.Aquamoeba;
import com.github.laxika.magicalvibes.cards.a.ArrogantWurm;
import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlashOfDefiance.class, ArrogantWurm.class, Aquamoeba.class, AvenTrooper.class})
class FlashOfDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Green and white creatures can't block this turn")
    void greenAndWhiteCreaturesCantBlock() {
        Permanent ownGreen = harness.addToBattlefieldAndReturn(player1, new ArrogantWurm());
        Permanent green = harness.addToBattlefieldAndReturn(player2, new ArrogantWurm());
        Permanent white = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        Permanent blue = harness.addToBattlefieldAndReturn(player2, new Aquamoeba());
        castFromHand();

        Permanent attacker = addCreatureReady(player1, new Aquamoeba());
        Permanent opposingAttacker = addCreatureReady(player2, new Aquamoeba());
        assertThat(bls.canBlockAttacker(gd, ownGreen, opposingAttacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, green, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, white, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, blue, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("A green creature cannot be declared as a blocker")
    void greenCreatureCannotBeDeclaredAsBlocker() {
        Permanent attacker = addCreatureReady(player1, new Aquamoeba());
        Permanent green = addCreatureReady(player2, new ArrogantWurm());
        castFromHand();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(green.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A blue creature can still be declared as a blocker")
    void blueCreatureCanBeDeclaredAsBlocker() {
        Permanent attacker = addCreatureReady(player1, new Aquamoeba());
        Permanent blue = addCreatureReady(player2, new Aquamoeba());
        castFromHand();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blue.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A green creature entering after resolution still can't block this turn")
    void greenCreatureEnteringAfterResolutionCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new Aquamoeba());
        castFromHand();
        Permanent green = addCreatureReady(player2, new ArrogantWurm());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(green.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Flashback pays 3 life and exiles Flash of Defiance")
    void flashbackPaysLifeAndExiles() {
        Permanent green = harness.addToBattlefieldAndReturn(player2, new ArrogantWurm());
        harness.setGraveyard(player1, List.of(new FlashOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new Aquamoeba());
        assertThat(bls.canBlockAttacker(gd, green, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        harness.assertNotInGraveyard(player1, "Flash of Defiance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Flash of Defiance"));
    }

    @Test
    @DisplayName("Flashback requires enough life")
    void flashbackRequiresEnoughLife() {
        gd.playerLifeTotals.put(player1.getId(), 2);
        harness.setGraveyard(player1, List.of(new FlashOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The restriction wears off at the end of the turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent green = harness.addToBattlefieldAndReturn(player2, new ArrogantWurm());
        Permanent attacker = addCreatureReady(player1, new Aquamoeba());
        castFromHand();

        assertThat(bls.canBlockAttacker(gd, green, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(bls.canBlockAttacker(gd, green, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private void castFromHand() {
        harness.castFromHand(player1, new FlashOfDefiance(), "{1}{R}");
        harness.passBothPriorities();
    }
}
