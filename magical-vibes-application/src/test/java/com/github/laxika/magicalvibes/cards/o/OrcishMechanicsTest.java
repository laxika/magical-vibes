package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishMechanics.class, GrizzlyBears.class, LeoninScimitar.class})
class OrcishMechanicsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact deals 2 damage to target player")
    void sacrificesArtifactAndDealsDamageToPlayer() {
        Permanent mechanics = addReadyMechanics(player1);
        addArtifact(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(mechanics.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can deal 2 damage to a target creature")
    void dealsDamageToTargetCreature() {
        addReadyMechanics(player1);
        addArtifact(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        addReadyMechanics(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMechanics(Player player) {
        return addCreatureReady(player, new OrcishMechanics());
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
    }
}
