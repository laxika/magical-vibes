package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HighwayRobberTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Highway Robber has correct card properties")
    void hasCorrectProperties() {
        HighwayRobber card = new HighwayRobber();

        assertThat(card.getName()).isEqualTo("Highway Robber");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.MERCENARY);
        assertThat(card.isNeedsTarget()).isTrue();
    }

    @Test
    @DisplayName("Has ETB drain life effect")
    void hasEtbEffect() {
        HighwayRobber card = new HighwayRobber();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class);
        TargetPlayerLosesLifeAndControllerGainsLifeEffect effect =
                (TargetPlayerLosesLifeAndControllerGainsLifeEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.lifeLoss()).isEqualTo(2);
        assertThat(effect.lifeGain()).isEqualTo(2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Highway Robber puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HighwayRobber()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Highway Robber");
    }

    // ===== Resolving creature spell =====

    @Test
    @DisplayName("Resolving puts Highway Robber on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Highway Robber"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Highway Robber");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(player2.getId());
    }

    // ===== ETB life drain =====

    @Test
    @DisplayName("ETB trigger causes target opponent to lose 2 life and controller to gain 2 life")
    void etbDrainsLife() {
        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("ETB drain works with non-default life totals")
    void etbDrainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records both life loss and life gain")
    void gameLogRecordsLifeChanges() {
        castHighwayRobber();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.gameLog).anyMatch(log -> log.contains("loses 2 life"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains 2 life"));
    }

    // ===== Helpers =====

    private void castHighwayRobber() {
        harness.setHand(player1, List.of(new HighwayRobber()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
    }
}
