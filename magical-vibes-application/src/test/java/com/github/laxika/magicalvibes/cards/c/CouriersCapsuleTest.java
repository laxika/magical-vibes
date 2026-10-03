package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({CouriersCapsule.class, Island.class})
class CouriersCapsuleTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices Courier's Capsule as a cost")
    void activatingSacrificesCapsule() {
        addReadyCapsule(player1);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Courier's Capsule");
        harness.assertInGraveyard(player1, "Courier's Capsule");
    }

    @Test
    @DisplayName("Resolving ability draws two cards")
    void resolvingDrawsTwoCards() {
        addReadyCapsule(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyCapsule(player1);
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent capsule = addReadyCapsule(player1);
        capsule.tap();
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addCapsuleMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature artifact can activate while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new CouriersCapsule());
        perm.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Courier's Capsule");
    }

    @Test
    @DisplayName("Cannot pay the blue mana requirement with colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent capsule = addReadyCapsule(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(capsule.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Courier's Capsule");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately but drawing waits for resolution")
    void drawingWaitsForResolution() {
        Permanent capsule = addReadyCapsule(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addCapsuleMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(capsule.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Courier's Capsule");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void addCapsuleMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyCapsule(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CouriersCapsule());
        perm.setSummoningSick(false);
        return perm;
    }
}
