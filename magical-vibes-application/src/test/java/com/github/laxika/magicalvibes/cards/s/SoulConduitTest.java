package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulConduit.class, PlatinumEmperion.class})
class SoulConduitTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges life totals between two players")
    void exchangesLifeTotals() {
        addConduitReady(player1);
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
    }

    @Test
    @DisplayName("Exchanges life totals when controller is at higher life")
    void exchangesWhenControllerHigher() {
        addConduitReady(player1);
        harness.setLife(player1, 30);
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 30);
    }

    @Test
    @DisplayName("Exchange with equal life totals results in no change")
    void exchangeWithEqualLifeTotals() {
        addConduitReady(player1);
        harness.setLife(player1, 15);
        harness.setLife(player2, 15);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack with both player targets")
    void activatingAbilityPutsOnStack() {
        addConduitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetIds()).containsExactly(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addConduitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating the ability taps Soul Conduit")
    void activatingTapsConduit() {
        Permanent conduit = addConduitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThat(conduit.isTapped()).isFalse();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));

        assertThat(conduit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent conduit = addConduitReady(player1);
        conduit.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Must target exactly two players")
    void mustTargetTwoPlayers() {
        addConduitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target same player twice")
    void cannotTargetSamePlayerTwice() {
        addConduitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("Exchange does not occur when first targeted player's life can't change")
    void exchangeBlockedWhenFirstPlayerLifeCantChange() {
        addConduitReady(player1);
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        // Neither life total changes
        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Exchange does not occur when second targeted player's life can't change")
    void exchangeBlockedWhenSecondPlayerLifeCantChange() {
        addConduitReady(player1);
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        // Neither life total changes
        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Exchange does not occur when both players' life totals can't change")
    void exchangeBlockedWhenBothPlayersLifeCantChange() {
        addConduitReady(player1);
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        // Neither life total changes
        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Uses life totals at resolution, even with reversed target order")
    void exchangesCurrentLifeTotals() {
        addConduitReady(player1);
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), player1.getId()));
        harness.setLife(player1, 8);
        harness.setLife(player2, 17);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("A newly entered noncreature artifact can activate its tap ability")
    void canActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new SoulConduit());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void cannotTargetPermanent() {
        Permanent conduit = addConduitReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), conduit.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addConduitReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SoulConduit());
        perm.setSummoningSick(false);
        return perm;
    }
}
