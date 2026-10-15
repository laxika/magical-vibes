package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HierophantsChalice.class})
class HierophantsChaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as an artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HierophantsChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(HierophantsChalice.class);
    }

    @Test
    @DisplayName("Resolving puts Hierophant's Chalice on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castChalice();
        harness.passBothPriorities(); // resolve artifact spell

        harness.assertOnBattlefield(player1, "Hierophant's Chalice");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(HierophantsChalice.class);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger causes target opponent to lose 1 life and controller to gain 1 life")
    void etbDrainsLife() {
        castChalice();
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("ETB drain works with non-default life totals")
    void etbDrainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        castChalice();
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertLife(player2, 14);
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castChalice();
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for mana adds one colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new HierophantsChalice());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entry trigger offers only an opponent as a target")
    void entryTriggerTargetsOnlyOpponent() {
        harness.enterBattlefieldAndReturn(player1, new HierophantsChalice());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Mana can be produced immediately while the entry trigger is on the stack")
    void manaAbilityResolvesImmediatelyWithEntryTriggerPending() {
        castChalice();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A tapped Chalice cannot produce additional mana")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new HierophantsChalice());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castChalice() {
        harness.setHand(player1, List.of(new HierophantsChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0, player2.getId());
    }
}
