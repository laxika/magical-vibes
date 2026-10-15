package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukRelentless;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollectiveDefiance.class, GrizzlyBears.class, Shock.class, GarrukRelentless.class})
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
    @DisplayName("Opponent mode rejects the controller as its target")
    void opponentModeRejectsController() {
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{2}, List.of(player1.getId()), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent or planeswalker");
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

    @Test
    @DisplayName("All three modes resolve with two escalate payments and a shared player target")
    void allThreeModesResolve() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        CollectiveDefiance discarded = new CollectiveDefiance();
        CollectiveDefiance drawn = new CollectiveDefiance();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(player2.getId(), bears.getId(), player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded, bears.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Wheel mode can target its controller and counts the hand after casting")
    void wheelModeCanTargetController() {
        CollectiveDefiance spell = new CollectiveDefiance();
        CollectiveDefiance discarded = new CollectiveDefiance();
        CollectiveDefiance drawn = new CollectiveDefiance();
        harness.setHand(player1, List.of(spell, discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player1.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(discarded.getId(), spell.getId());
    }

    @Test
    @DisplayName("Wheel mode with an empty hand draws no cards")
    void emptyHandDrawsNothing() {
        harness.setHand(player2, List.of());
        CollectiveDefiance libraryCard = new CollectiveDefiance();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The third mode can damage a planeswalker controlled by the caster")
    void thirdModeCanTargetOwnPlaneswalker() {
        Permanent garruk = harness.enterBattlefieldAndReturn(player1, new GarrukRelentless());
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(garruk.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(garruk);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(garruk.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An absent creature target does not stop damage to the legal opponent target")
    void remainingLegalModeStillResolves() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                List.of(bears.getId(), player2.getId()), null);
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
