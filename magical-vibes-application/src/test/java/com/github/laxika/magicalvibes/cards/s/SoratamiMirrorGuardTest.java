package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoratamiMirrorGuard.class, Island.class, IsamaruHoundOfKonda.class, MossKami.class})
class SoratamiMirrorGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost and makes a power 2 creature unblockable")
    void makesTargetUnblockable() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player1, new Island());
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, hound.getId());

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));

        harness.passBothPriorities();

        assertThat(hound.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A creature with power greater than 2 is an illegal target")
    void rejectsCreatureWithPowerGreaterThanTwo() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player1, new Island());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new MossKami());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land is an illegal target")
    void rejectsLandTarget() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player1, new Island());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0,
                opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player1, new Island());
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, hound.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player2, new Island());
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, hound.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player1, new Island());
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        // Empty the hand so the returned Island cannot push player1 over the cleanup-step hand limit.
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, hound.getId());
        harness.passBothPriorities();
        assertThat(hound.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hound.isCantBeBlocked()).isFalse();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
