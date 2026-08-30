package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RimewindTaskmageTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped target permanent with four snow permanents")
    void tapsUntappedTargetPermanent() {
        addReadyTaskmage(player1);
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped target permanent with four snow permanents")
    void untapsTappedTargetPermanent() {
        addReadyTaskmage(player1);
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the may choice leaves the target unchanged")
    void decliningMayChoiceLeavesTargetUnchanged() {
        addReadyTaskmage(player1);
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without four snow permanents you control")
    void cannotActivateWithoutFourSnowPermanentsYouControl() {
        addReadyTaskmage(player1);
        addSnowPermanents(player1, 3);
        addSnowPermanents(player2, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more snow permanents");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addReadyTaskmage(player1);
        addSnowPermanents(player1, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTaskmage(Player player) {
        Permanent taskmage = new Permanent(new RimewindTaskmage());
        taskmage.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(taskmage);
        return taskmage;
    }

    private void addSnowPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent snow = new Permanent(new GrizzlyBears());
            TestCards.mutableCard(snow).setSupertypes(EnumSet.of(CardSupertype.SNOW));
            gd.playerBattlefields.get(player.getId()).add(snow);
        }
    }
}
