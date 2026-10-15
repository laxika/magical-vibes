package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.s.SimicRagworm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Twinstrike.class, AssaultZeppelid.class, SimicRagworm.class, AzoriusSignet.class})
class TwinstrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each target creature when the controller has cards in hand")
    void dealsDamageWithCardsInHand() {
        Permanent zeppelid = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent ragworm = harness.addToBattlefieldAndReturn(player2, new SimicRagworm());
        harness.setHand(player1, List.of(new Twinstrike(), new Twinstrike()));
        giveMana();

        harness.castAndResolveInstant(player1, 1, List.of(zeppelid.getId(), ragworm.getId()));

        assertThat(zeppelid.getMarkedDamage()).isEqualTo(2);
        assertThat(ragworm.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Assault Zeppelid");
        harness.assertOnBattlefield(player2, "Simic Ragworm");
    }

    @Test
    @DisplayName("Destroys both target creatures with an empty hand")
    void destroysTargetsWithEmptyHand() {
        Permanent zeppelid = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent ragworm = harness.addToBattlefieldAndReturn(player2, new SimicRagworm());
        harness.setHand(player1, List.of(new Twinstrike()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(zeppelid.getId(), ragworm.getId()));

        harness.assertInGraveyard(player2, "Assault Zeppelid");
        harness.assertInGraveyard(player2, "Simic Ragworm");
    }

    @Test
    @DisplayName("Checks hellbent when the spell resolves")
    void checksHellbentAtResolution() {
        Permanent zeppelid = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent ragworm = harness.addToBattlefieldAndReturn(player2, new SimicRagworm());
        harness.setHand(player1, List.of(new Twinstrike(), new Twinstrike()));
        giveMana();

        harness.castInstant(player1, 0, List.of(zeppelid.getId(), ragworm.getId()));
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Assault Zeppelid");
        harness.assertInGraveyard(player2, "Simic Ragworm");
    }

    @Test
    @DisplayName("Resolves against the remaining legal creature target")
    void resolvesWithOneRemainingLegalTarget() {
        Permanent zeppelid = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent ragworm = harness.addToBattlefieldAndReturn(player2, new SimicRagworm());
        harness.setHand(player1, List.of(new Twinstrike()));
        giveMana();

        harness.castInstant(player1, 0, List.of(zeppelid.getId(), ragworm.getId()));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ragworm));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Assault Zeppelid");
    }

    @Test
    @DisplayName("Requires exactly two creature targets")
    void rejectsWrongTargetCountAndType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Twinstrike()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), signet.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals damage if the controller gains a card before resolution")
    void losesHellbentBeforeResolution() {
        Permanent zeppelid = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent ragworm = harness.addToBattlefieldAndReturn(player2, new SimicRagworm());
        harness.setHand(player1, List.of(new Twinstrike()));
        giveMana();

        harness.castInstant(player1, 0, List.of(zeppelid.getId(), ragworm.getId()));
        harness.setHand(player1, List.of(new AzoriusSignet()));
        harness.passBothPriorities();

        assertThat(zeppelid.getMarkedDamage()).isEqualTo(2);
        assertThat(ragworm.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Assault Zeppelid");
        harness.assertOnBattlefield(player2, "Simic Ragworm");
    }

    @Test
    @DisplayName("Deals damage to the remaining legal target without hellbent")
    void damagesRemainingLegalTarget() {
        Permanent zeppelid = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent ragworm = harness.addToBattlefieldAndReturn(player2, new SimicRagworm());
        harness.setHand(player1, List.of(new Twinstrike(), new AzoriusSignet()));
        giveMana();

        harness.castInstant(player1, 0, List.of(zeppelid.getId(), ragworm.getId()));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ragworm));
        harness.passBothPriorities();

        assertThat(zeppelid.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Assault Zeppelid");
    }

    @Test
    @DisplayName("Hellbent destroys targets controlled by different players")
    void destroysTargetsWithDifferentControllers() {
        Permanent zeppelid = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        Permanent ragworm = harness.addToBattlefieldAndReturn(player2, new SimicRagworm());
        harness.setHand(player1, List.of(new Twinstrike()));
        harness.setHand(player2, List.of(new AzoriusSignet()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(zeppelid.getId(), ragworm.getId()));

        harness.assertInGraveyard(player1, "Assault Zeppelid");
        harness.assertInGraveyard(player2, "Simic Ragworm");
    }
    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
