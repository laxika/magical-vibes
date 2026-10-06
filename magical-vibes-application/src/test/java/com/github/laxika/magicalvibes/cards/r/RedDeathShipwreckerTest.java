package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedDeathShipwrecker.class})
class RedDeathShipwreckerTest extends BaseCardTest {

    @Test
    @DisplayName("Goads an opponent's creature, makes its controller draw, and adds red mana")
    void goadsCreatureDrawsForItsControllerAndAddsRedMana() {
        Permanent source = addReadySource(player1);
        Permanent target = addCreatureReady(player2, new RedDeathShipwrecker());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new RedDeathShipwrecker()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = addReadySource(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityUsesTheStackAndDoesNotProduceManaImmediately() {
        Permanent source = addReadySource(player1);
        Permanent target = addReadySource(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new RedDeathShipwrecker()));

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void disappearingTargetPreventsDrawAndMana() {
        Permanent source = addReadySource(player1);
        Permanent target = addReadySource(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new RedDeathShipwrecker()));

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addReadySource(player1);
        Permanent target = addReadySource(player2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new RedDeathShipwrecker()));

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void tappedGoadedCreatureIsNotRequiredToAttack() {
        addReadySource(player1);
        Permanent target = addReadySource(player2);
        harness.setLibrary(player2, List.of(new RedDeathShipwrecker()));
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.tap();

        declareAttackers(player2, List.of());

        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    void goadedCreatureCanAttackTheGoadingPlayerInTwoPlayerGame() {
        addReadySource(player1);
        Permanent target = addReadySource(player2);
        harness.setLibrary(player2, List.of(new RedDeathShipwrecker()));
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThat(target.isAttacking()).isTrue();
    }

    private Permanent addReadySource(Player player) {
        return addCreatureReady(player, new RedDeathShipwrecker());
    }
}
