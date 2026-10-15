package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrMindservant.class})
class MyrMindservantTest extends BaseCardTest {

    @Test
    @DisplayName("Activating taps the Myr and puts its ability on the stack")
    void activateTapsAndPutsAbilityOnStack() {
        Permanent myr = addCreatureReady(player1, new MyrMindservant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(myr.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving shuffles the controller's library")
    void resolvingShufflesOwnLibrary() {
        addCreatureReady(player1, new MyrMindservant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without the required mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new MyrMindservant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Generic activation cost can be paid with colored mana")
    void genericActivationCostCanUseColoredMana() {
        addCreatureReady(player1, new MyrMindservant());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate while the Myr has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new MyrMindservant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(myr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new MyrMindservant());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty library can still be shuffled")
    void resolvesWithEmptyLibrary() {
        addCreatureReady(player1, new MyrMindservant());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    @Test
    @DisplayName("The ability resolves after its source leaves and preserves both libraries' cards")
    void resolvesAfterSourceLeavesWithoutAffectingOpponentLibrary() {
        Permanent myr = addCreatureReady(player1, new MyrMindservant());
        List<MyrMindservant> ownLibrary = List.of(new MyrMindservant(), new MyrMindservant());
        List<MyrMindservant> opponentLibrary = List.of(new MyrMindservant(), new MyrMindservant());
        harness.setLibrary(player1, ownLibrary);
        harness.setLibrary(player2, opponentLibrary);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(myr);
        harness.setGraveyard(player1, List.of(myr.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(ownLibrary);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains(player1.getUsername() + " shuffles their library")).isTrue();
    }
}
