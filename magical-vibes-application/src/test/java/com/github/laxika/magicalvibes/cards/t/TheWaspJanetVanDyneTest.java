package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWaspJanetVanDyne.class, GrizzlyBears.class})
class TheWaspJanetVanDyneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to a tapped creature an opponent controls")
    void etbDealsFourDamageToTappedOpponentCreature() {
        Permanent target = addTappedCreature(player2);
        castWasp(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new TheWaspJanetVanDyne()));
        addWaspMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Cannot target a tapped creature its controller controls")
    void cannotTargetOwnTappedCreature() {
        Permanent target = addTappedCreature(player1);
        harness.setHand(player1, List.of(new TheWaspJanetVanDyne()));
        addWaspMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("ETB does not damage the target if it becomes untapped before resolution")
    void etbFizzlesWhenTargetBecomesUntapped() {
        Permanent target = addTappedCreature(player2);
        castWasp(target.getId());

        harness.passBothPriorities();
        target.untap();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB deals exactly 4 damage to a creature that survives")
    void etbDealsExactlyFourDamage() {
        Permanent target = addTappedCreature(player2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        castWasp(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Wasp can enter when there are no legal targets")
    void entersWithNoLegalTargets() {
        addCreature(player2);
        addTappedCreature(player1);
        harness.castFromHand(player1, new TheWaspJanetVanDyne(), "{2}{W}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Wasp, Janet Van Dyne");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB does not damage a target that comes under its controller's control")
    void etbFizzlesWhenTargetChangesController() {
        Permanent target = addTappedCreature(player2);
        castWasp(target.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Entering without being cast still triggers the damage ability")
    void enteringWithoutCastingDealsDamage() {
        Permanent target = addTappedCreature(player2);
        harness.enterBattlefieldAndReturn(player1, new TheWaspJanetVanDyne());
        harness.handlePermanentChosen(player1, target.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB damage resolves after The Wasp leaves the battlefield")
    void etbResolvesWithoutItsSource() {
        Permanent target = addTappedCreature(player2);
        castWasp(target.getId());

        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "The Wasp, Janet Van Dyne");
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(sourceId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = addCreature(player);
        permanent.tap();
        return permanent;
    }

    private void castWasp(UUID targetId) {
        harness.setHand(player1, List.of(new TheWaspJanetVanDyne()));
        addWaspMana();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addWaspMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
