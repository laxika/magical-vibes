package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessReveler.class, LeoninScimitar.class, GrizzlyBears.class})
class RecklessRevelerTest extends BaseCardTest {

    @Test
    void activationSacrificesReveler() {
        addReadyReveler(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Reckless Reveler");
        harness.assertInGraveyard(player1, "Reckless Reveler");
    }

    @Test
    void resolutionDestroysTargetArtifact() {
        addReadyReveler(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void cannotTargetCreature() {
        addReadyReveler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addReadyReveler(player1);
        Permanent target = addReadyArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWhileSummoningSick() {
        Permanent reveler = harness.addToBattlefieldAndReturn(player1, new RecklessReveler());
        reveler.setSummoningSick(true);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reckless Reveler");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void canActivateWhileTapped() {
        Permanent reveler = addReadyReveler(player1);
        reveler.tap();
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reckless Reveler");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void canDestroyOwnArtifact() {
        addReadyReveler(player1);
        Permanent target = addReadyArtifact(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reckless Reveler");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    void wrongColorManaCannotPayActivationCost() {
        addReadyReveler(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Reckless Reveler");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    private Permanent addReadyReveler(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new RecklessReveler());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
