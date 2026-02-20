package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FesteringGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GravebornMuseTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Graveborn Muse has correct card properties")
    void hasCorrectProperties() {
        GravebornMuse card = new GravebornMuse();

        assertThat(card.getName()).isEqualTo("Graveborn Muse");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.SPIRIT);
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst()).isInstanceOf(DrawAndLoseLifePerSubtypeEffect.class);
    }

    // ===== Triggering =====

    @Test
    @DisplayName("Draws 1 and loses 1 life when only Graveborn Muse is the only Zombie")
    void drawsAndLosesLifeForSelfAsZombie() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Draws and loses life equal to total Zombie count")
    void drawsAndLosesLifeEqualToZombieCount() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // 2 Zombies: Graveborn Muse + Festering Goblin
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Non-Zombie creatures do not increase the count")
    void nonZombiesDoNotCount() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Only Graveborn Muse is a Zombie, Grizzly Bears is a Bear
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2); // opponent's upkeep
        harness.passBothPriorities();

        // No trigger — player1's hand and life unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Only counts Zombies controller controls, not opponent's")
    void onlyCountsOwnZombies() {
        harness.addToBattlefield(player1, new GravebornMuse());
        harness.addToBattlefield(player2, new FesteringGoblin()); // opponent's Zombie
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Only 1 Zombie (Graveborn Muse) — opponent's Festering Goblin doesn't count
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }
}

