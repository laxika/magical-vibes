package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicStrike.class, RuneclawBear.class, Mountain.class, Plains.class})
class SeismicStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Seismic Strike targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Seismic Strike deals damage equal to Mountains you control")
    void dealsDamageEqualToControlledMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Seismic Strike counts only your Mountains, not opponent Mountains")
    void countsOnlyControllersMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Seismic Strike counts Mountains at resolution")
    void countsMountainsAtResolution() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castInstant(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Mountain"));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Seismic Strike counts Mountains added after casting")
    void countsMountainsAddedAfterCasting() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Runeclaw Bear"));
        harness.addToBattlefield(player1, new Mountain());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Seismic Strike can target a creature you control")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Runeclaw Bear"));

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Seismic Strike deals no damage with no Mountains")
    void dealsNoDamageWithoutMountains() {
        var target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Seismic Strike");
    }

    @Test
    @DisplayName("Seismic Strike cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Seismic Strike cannot target a noncreature land")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Seismic Strike marks exactly one damage with one Mountain")
    void marksExactlyOneDamage() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Mountain());
        var target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new SeismicStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }
}
