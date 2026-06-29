package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StormFleetSpyTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has raid-conditional ETB draw effect")
    void hasRaidEtbDrawEffect() {
        StormFleetSpy card = new StormFleetSpy();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(RaidConditionalEffect.class);

        RaidConditionalEffect raid =
                (RaidConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(raid.wrapped()).isInstanceOf(DrawCardEffect.class);
    }

    // ===== ETB with raid met =====

    @Test
    @DisplayName("ETB triggers draw when raid is met (attacked this turn)")
    void etbTriggersWithRaid() {
        markAttackedThisTurn();
        castStormFleetSpy();
        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Storm Fleet Spy");
    }

    @Test
    @DisplayName("ETB raid trigger draws a card for controller")
    void etbDrawsCardWithRaid() {
        markAttackedThisTurn();
        castStormFleetSpy();
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast + 1);
    }

    // ===== ETB without raid =====

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        castStormFleetSpy();
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Creature is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Storm Fleet Spy"));

        // Hand size unchanged (no draw)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast);
    }

    // ===== Raid lost before resolution (intervening-if) =====

    @Test
    @DisplayName("ETB does nothing if raid condition is lost before resolution")
    void etbFizzlesWhenRaidLost() {
        markAttackedThisTurn();
        castStormFleetSpy();
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove the raid flag before ETB resolves
        gd.playersDeclaredAttackersThisTurn.clear();

        harness.passBothPriorities(); // resolve ETB trigger — raid no longer met

        // Hand size unchanged (no draw)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("raid ability does nothing"));
    }

    // ===== Creature enters battlefield regardless =====

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersWithoutRaid() {
        castStormFleetSpy();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Storm Fleet Spy"));
    }

    @Test
    @DisplayName("Stack is empty after full resolution with raid")
    void stackEmptyAfterResolution() {
        markAttackedThisTurn();
        castStormFleetSpy();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castStormFleetSpy() {
        harness.setHand(player1, List.of(new StormFleetSpy()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    }
}
