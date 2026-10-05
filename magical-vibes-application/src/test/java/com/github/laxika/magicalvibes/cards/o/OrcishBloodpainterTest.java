package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishBloodpainter.class, GrizzlyBears.class, LlanowarElves.class})
class OrcishBloodpainterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature deals 1 damage to a player")
    void sacrificesCreatureAndDealsDamageToPlayer() {
        addReadyBloodpainter(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void dealsDamageToTargetCreature() {
        addReadyBloodpainter(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("A creature can sacrifice itself to pay the ability's cost")
    void canSacrificeItself() {
        Permanent bloodpainter = addReadyBloodpainter(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Orcish Bloodpainter");
        harness.assertLife(player2, 19);
        assertThat(bloodpainter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Bloodpainter cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent bloodpainter = addReadyBloodpainter(player1);
        bloodpainter.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Orcish Bloodpainter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Bloodpainter cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent bloodpainter = addReadyBloodpainter(player1);
        bloodpainter.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Orcish Bloodpainter");
        assertThat(bloodpainter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodpainter may target itself and sacrifice itself, leaving an illegal target")
    void canTargetAndSacrificeItself() {
        Permanent bloodpainter = addReadyBloodpainter(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, bloodpainter.getId());

        harness.assertInGraveyard(player1, "Orcish Bloodpainter");
        harness.assertNotOnBattlefield(player1, "Orcish Bloodpainter");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyBloodpainter(Player player) {
        return addCreatureReady(player, new OrcishBloodpainter());
    }
}
