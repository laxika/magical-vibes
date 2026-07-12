package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SinkingFeelingTest extends BaseCardTest {

    // ===== Targeting =====

    @Test
    @DisplayName("Can enchant any creature")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new SinkingFeeling()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Doesn't untap during controller's untap step =====

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new SinkingFeeling());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again after Sinking Feeling is removed")
    void creatureUntapsAfterRemoval() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new SinkingFeeling());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    // ===== Granted ability: untap by putting a -1/-1 counter =====

    @Test
    @DisplayName("Enchanted creature can pay {1} and a -1/-1 counter to untap itself")
    void grantedAbilityUntapsWithMinusCounter() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new SinkingFeeling());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        // Untapped by the ability
        assertThat(bears.isTapped()).isFalse();
        // A -1/-1 counter shrinks the 2/2 Grizzly Bears to 1/1
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    // ===== Ability is lost when the aura leaves =====

    @Test
    @DisplayName("Creature loses the granted untap ability when Sinking Feeling is removed")
    void abilityLostWhenRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new SinkingFeeling());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
