package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentPermanentsAndPutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyRandomOpponentPermanentWithCounterEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HaphazardBombardmentTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect to choose opponent permanents and put aim counters")
    void hasCorrectETBEffect() {
        HaphazardBombardment card = new HaphazardBombardment();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseOpponentPermanentsAndPutCountersEffect.class);
    }

    @Test
    @DisplayName("Has controller end step triggered destroy effect")
    void hasCorrectEndStepEffect() {
        HaphazardBombardment card = new HaphazardBombardment();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(DestroyRandomOpponentPermanentWithCounterEffect.class);
    }

    // ===== ETB: aim counter placement =====

    @Test
    @DisplayName("ETB puts aim counters on all opponent nonenchantment permanents when 4 or fewer")
    void etbPutsAimCountersOnAllWhenFourOrFewer() {
        Permanent bears1 = addCreature(player2, new GrizzlyBears());
        Permanent bears2 = addCreature(player2, new GrizzlyBears());

        castHaphazardBombardment(player1);

        // Aim counters should be placed on both opponent creatures
        assertThat(bears1.getAimCounters()).isEqualTo(1);
        assertThat(bears2.getAimCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB puts aim counters on exactly 4 when opponent has exactly 4 nonenchantment permanents")
    void etbPutsAimCountersOnExactlyFour() {
        Permanent c1 = addCreature(player2, new GrizzlyBears());
        Permanent c2 = addCreature(player2, new GrizzlyBears());
        Permanent c3 = addCreature(player2, new GrizzlyBears());
        Permanent c4 = addCreature(player2, new GrizzlyBears());

        castHaphazardBombardment(player1);

        assertThat(c1.getAimCounters()).isEqualTo(1);
        assertThat(c2.getAimCounters()).isEqualTo(1);
        assertThat(c3.getAimCounters()).isEqualTo(1);
        assertThat(c4.getAimCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not put aim counters on enchantments")
    void etbSkipsEnchantments() {
        Permanent bears = addCreature(player2, new GrizzlyBears());
        Permanent enchantment = addEnchantment(player2);

        castHaphazardBombardment(player1);

        assertThat(bears.getAimCounters()).isEqualTo(1);
        assertThat(enchantment.getAimCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB does nothing when opponent has no nonenchantment permanents")
    void etbDoesNothingWhenNoEligiblePermanents() {
        // Only enchantments on opponent's side
        addEnchantment(player2);

        castHaphazardBombardment(player1);

        // No counters should be placed — card still on battlefield
        harness.assertOnBattlefield(player1, "Haphazard Bombardment");
    }

    @Test
    @DisplayName("ETB does not put aim counters on own permanents")
    void etbDoesNotAffectOwnPermanents() {
        Permanent ownBears = addCreature(player1, new GrizzlyBears());
        Permanent oppBears = addCreature(player2, new GrizzlyBears());

        castHaphazardBombardment(player1);

        assertThat(ownBears.getAimCounters()).isEqualTo(0);
        assertThat(oppBears.getAimCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB puts aim counters on opponent lands (nonenchantment permanents)")
    void etbPutsCountersOnLands() {
        Permanent land = addLand(player2);

        castHaphazardBombardment(player1);

        assertThat(land.getAimCounters()).isEqualTo(1);
    }

    // ===== End step trigger =====

    @Test
    @DisplayName("End step trigger destroys one permanent with aim counter when 2+ have counters")
    void endStepDestroysOneWhenTwoOrMoreHaveCounters() {
        Permanent bears1 = addCreature(player2, new GrizzlyBears());
        Permanent bears2 = addCreature(player2, new GrizzlyBears());
        bears1.setAimCounters(1);
        bears2.setAimCounters(1);

        addBombardmentToBattlefield(player1);
        advanceToEndStep(player1);

        // One trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        // Exactly one of the two should be destroyed
        int remaining = (int) gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
        assertThat(remaining).isEqualTo(1);
    }

    @Test
    @DisplayName("End step trigger does not fire when fewer than 2 permanents have aim counters")
    void endStepDoesNotFireWhenFewerThanTwoCounters() {
        Permanent bears = addCreature(player2, new GrizzlyBears());
        bears.setAimCounters(1);

        addBombardmentToBattlefield(player1);
        advanceToEndStep(player1);

        // No trigger should be on the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("End step trigger does not fire when no permanents have aim counters")
    void endStepDoesNotFireWhenNoCounters() {
        addCreature(player2, new GrizzlyBears());

        addBombardmentToBattlefield(player1);
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("End step trigger re-checks condition on resolution — fizzles if only 1 left")
    void endStepFizzlesIfConditionNoLongerMet() {
        Permanent bears1 = addCreature(player2, new GrizzlyBears());
        Permanent bears2 = addCreature(player2, new GrizzlyBears());
        bears1.setAimCounters(1);
        bears2.setAimCounters(1);

        addBombardmentToBattlefield(player1);
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);

        // Remove one of the aim-countered permanents before resolution
        gd.playerBattlefields.get(player2.getId()).remove(bears2);

        int sizeBefore = gd.playerBattlefields.get(player2.getId()).size();
        harness.passBothPriorities(); // resolve trigger

        // Should fizzle — only 1 permanent with aim counter remains (need 2)
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(sizeBefore);
    }

    @Test
    @DisplayName("End step trigger only fires on controller's end step")
    void endStepOnlyFiresOnControllersEndStep() {
        Permanent bears1 = addCreature(player2, new GrizzlyBears());
        Permanent bears2 = addCreature(player2, new GrizzlyBears());
        bears1.setAimCounters(1);
        bears2.setAimCounters(1);

        addBombardmentToBattlefield(player1);

        // Advance to player2's end step (not the controller's)
        advanceToEndStep(player2);

        // Should not trigger on opponent's end step
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private void castHaphazardBombardment(Player player) {
        harness.setHand(player, List.of(new HaphazardBombardment()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.castEnchantment(player, 0);
        harness.passBothPriorities(); // resolve enchantment spell — enters battlefield, ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB trigger — places aim counters
    }

    private Permanent addBombardmentToBattlefield(Player player) {
        HaphazardBombardment card = new HaphazardBombardment();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEnchantment(Player player) {
        Card enchantment = new Card();
        enchantment.setType(CardType.ENCHANTMENT);
        enchantment.setName("Test Enchantment");
        Permanent perm = new Permanent(enchantment);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLand(Player player) {
        Card land = new Card();
        land.setType(CardType.LAND);
        land.setName("Test Land");
        Permanent perm = new Permanent(land);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN → END_STEP, triggers fire
    }
}
