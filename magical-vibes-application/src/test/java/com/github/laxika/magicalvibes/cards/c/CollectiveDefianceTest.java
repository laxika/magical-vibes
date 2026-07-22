package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectiveDefianceTest extends BaseCardTest {

    // Modes: 0 = wheel target player, 1 = 4 damage to creature, 2 = 3 damage to opponent/PW

    @Test
    @DisplayName("Wheel mode: target discards hand then draws that many")
    void wheelModeDiscardsThenDraws() {
        harness.setHand(player2, new ArrayList<>(List.of(new Shock(), new Shock())));
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 2);
    }

    @Test
    @DisplayName("Creature mode: deals 4 damage to target creature")
    void creatureModeDealsFourDamage() {
        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        Permanent bears = addCreatureReady(player2, bigBear);
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(bears.getId()), null);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent mode: deals 3 damage to target opponent")
    void opponentModeDealsThreeDamage() {
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Two modes: escalate {1} and both effects resolve")
    void twoModesEscalateAndResolve() {
        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        Permanent bears = addCreatureReady(player2, bigBear);
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                List.of(bears.getId(), player2.getId()), null);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Two modes without escalate mana is rejected")
    void twoModesWithoutEscalateManaRejected() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                        List.of(bears.getId(), player2.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
