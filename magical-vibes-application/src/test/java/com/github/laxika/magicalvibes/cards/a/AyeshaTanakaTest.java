package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AyeshaTanakaTest extends BaseCardTest {

    private void addReadyAyesha() {
        harness.addToBattlefieldAndReturn(player1, new AyeshaTanaka()).setSummoningSick(false);
    }

    private RodOfRuin putRodAbilityOnStack() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        return rod;
    }

    @Test
    @DisplayName("Allows an artifact ability to resolve when its controller pays {W}")
    void allowsWhitePayment() {
        addReadyAyesha();
        RodOfRuin rod = putRodAbilityOnStack();
        harness.addMana(player2, ManaColor.WHITE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, rod.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, lifeBefore - 1);
        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).get(ManaColor.WHITE))
                .isZero();
    }

    @Test
    @DisplayName("Counters an artifact ability when its controller declines to pay")
    void declinesWhitePayment() {
        addReadyAyesha();
        RodOfRuin rod = putRodAbilityOnStack();
        harness.addMana(player2, ManaColor.WHITE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, rod.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, lifeBefore);
        harness.assertOnBattlefield(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Colorless mana cannot pay the white ransom")
    void requiresWhiteMana() {
        addReadyAyesha();
        RodOfRuin rod = putRodAbilityOnStack();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, rod.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().pendingMayAbilities).isEmpty();
        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an activated ability from a nonartifact source")
    void cannotTargetNonartifactAbility() {
        addReadyAyesha();
        ProdigalSorcerer sorcerer = new ProdigalSorcerer();
        harness.addToBattlefieldAndReturn(player2, sorcerer).setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        UUID sorcererId = sorcerer.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sorcererId))
                .isInstanceOf(IllegalStateException.class);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore - 1);
    }
}
