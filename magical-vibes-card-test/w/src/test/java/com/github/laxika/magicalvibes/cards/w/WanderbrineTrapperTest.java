package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WanderbrineTrapper.class})
class WanderbrineTrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an opponent's creature after paying the ability's costs")
    void tapsOpponentCreature() {
        Permanent trapper = addReadyTrapper(player1);
        Permanent costCreature = addReadyTrapper(player1);
        Permanent target = addReadyTrapper(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(trapper.isTapped()).isTrue();
        assertThat(costCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the cost by tapping Wanderbrine Trapper itself")
    void cannotTapSourceForAnotherCreatureCost() {
        addReadyTrapper(player1);
        Permanent target = addReadyTrapper(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its controller")
    void rejectsControllerCreatureTarget() {
        addReadyTrapper(player1);
        addReadyTrapper(player1);
        Permanent target = addReadyTrapper(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick creature can pay the additional tap cost")
    void canTapSummoningSickSupportCreature() {
        Permanent trapper = addReadyTrapper(player1);
        Permanent costCreature = harness.addToBattlefieldAndReturn(player1, new WanderbrineTrapper());
        costCreature.setSummoningSick(true);
        Permanent target = addReadyTrapper(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(trapper.isTapped()).isTrue();
        assertThat(costCreature.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Trapper cannot pay its own tap-symbol cost")
    void summoningSickTrapperCannotActivate() {
        Permanent trapper = addReadyTrapper(player1);
        trapper.setSummoningSick(true);
        addReadyTrapper(player1);
        Permanent target = addReadyTrapper(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already-tapped creature cannot pay the additional tap cost")
    void cannotTapAlreadyTappedSupportCreature() {
        addReadyTrapper(player1);
        Permanent costCreature = addReadyTrapper(player1);
        costCreature.tap();
        Permanent target = addReadyTrapper(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already-tapped Trapper cannot activate")
    void tappedTrapperCannotActivate() {
        Permanent trapper = addReadyTrapper(player1);
        trapper.tap();
        addReadyTrapper(player1);
        Permanent target = addReadyTrapper(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability requires one mana")
    void cannotActivateWithoutMana() {
        addReadyTrapper(player1);
        addReadyTrapper(player1);
        Permanent target = addReadyTrapper(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's tapped creature is still a legal target")
    void canTargetAlreadyTappedCreature() {
        Permanent trapper = addReadyTrapper(player1);
        Permanent costCreature = addReadyTrapper(player1);
        Permanent target = addReadyTrapper(player2);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isTrue();
        assertThat(costCreature.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that changes to its controller's battlefield is not tapped on resolution")
    void targetMustRemainAnOpponentsCreature() {
        Permanent trapper = addReadyTrapper(player1);
        Permanent costCreature = addReadyTrapper(player1);
        Permanent target = addReadyTrapper(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(trapper.isTapped()).isTrue();
        assertThat(costCreature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyTrapper(Player player) {
        return addCreatureReady(player, new WanderbrineTrapper());
    }
}
