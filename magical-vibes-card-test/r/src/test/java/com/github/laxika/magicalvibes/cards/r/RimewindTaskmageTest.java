package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimewindTaskmage.class, SnowCoveredIsland.class})
class RimewindTaskmageTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped target permanent with four snow permanents")
    void tapsUntappedTargetPermanent() {
        addCreatureReady(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Tap");
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped target permanent with four snow permanents")
    void untapsTappedTargetPermanent() {
        addCreatureReady(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Untap");
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the may choice leaves the target unchanged")
    void decliningMayChoiceLeavesTargetUnchanged() {
        addCreatureReady(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Offers a tap or untap choice after accepting the may ability")
    void offersTapOrUntapChoiceAtResolution() {
        addCreatureReady(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.handleListChoice(player1, "Untap");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without four snow permanents you control")
    void cannotActivateWithoutFourSnowPermanentsYouControl() {
        addCreatureReady(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 3);
        addSnowPermanents(player2, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more snow permanents");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Can target itself and untap after paying the tap cost")
    void canUntapItself() {
        Permanent taskmage = addCreatureReady(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, taskmage.getId());
        assertThat(taskmage.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Untap");

        assertThat(taskmage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Losing snow permanents after activation does not stop resolution")
    void snowRequirementIsOnlyCheckedAtActivation() {
        addCreatureReady(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Tap");

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick even with four snow permanents")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without paying one mana")
    void cannotActivateWithoutMana() {
        Permanent taskmage = addCreatureReady(player1, new RimewindTaskmage());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(taskmage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addSnowPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new SnowCoveredIsland());
        }
    }
}
