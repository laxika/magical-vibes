package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheHive;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UktabiOrangutan.class, TheHive.class, GrizzlyBears.class})
class UktabiOrangutanTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new TheHive());
        harness.setHand(player1, List.of(new UktabiOrangutan()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "The Hive");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Uktabi Orangutan");

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new TheHive());
        harness.setHand(player1, List.of(new UktabiOrangutan()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "The Hive");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "The Hive");
        harness.assertInGraveyard(player2, "The Hive");
    }

    @Test
    @DisplayName("ETB fizzles if target artifact is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new TheHive());
        harness.setHand(player1, List.of(new UktabiOrangutan()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "The Hive");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "The Hive");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UktabiOrangutan()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB can destroy an artifact controlled by its controller")
    void etbDestroysArtifactControlledByItsController() {
        harness.addToBattlefield(player1, new TheHive());
        harness.setHand(player1, List.of(new UktabiOrangutan()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "The Hive");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Hive");
        harness.assertInGraveyard(player1, "The Hive");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new UktabiOrangutan()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Uktabi Orangutan");
        assertThat(gd.stack).isEmpty();
    }
}
