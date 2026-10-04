package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostTrickster.class, GrizzlyBears.class})
class FrostTricksterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target creature and makes it skip its controller's next untap step")
    void tapsTargetCreatureAndSkipsNextUntap() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFrostTrickster(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new FrostTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownBearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the affected creature skips one untap step of its controller")
    void restrictionExpiresAfterOneControllerUntapStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        otherBear.setTapped(true);

        castFrostTrickster(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        assertThat(otherBear.isTapped()).isFalse();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Already tapped creatures still receive the untap restriction")
    void alreadyTappedTargetStillSkipsUntap() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setTapped(true);

        castFrostTrickster(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two restrictions before the same untap step do not skip two steps")
    void overlappingRestrictionsExpireTogether() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFrostTrickster(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();
        castFrostTrickster(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A target leaving before resolution does not affect a different creature")
    void departedTargetDoesNotRetarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFrostTrickster(player2, "Grizzly Bears");
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerGraveyards.get(player2.getId()).add(bears.getCard());
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(replacement.isTapped()).isFalse();
        assertThat(replacement.getSkipUntapCount()).isZero();
        harness.assertOnBattlefield(player1, "Frost Trickster");
    }

    @Test
    @DisplayName("Can enter the battlefield when no opponent controls a creature")
    void entersWithoutALegalTriggerTarget() {
        harness.setHand(player1, List.of(new FrostTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frost Trickster");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castFrostTrickster(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new FrostTrickster()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
