package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
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
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bloodpainter.isTapped()).isTrue();
    }

    private Permanent addReadyBloodpainter(Player player) {
        Permanent bloodpainter = new Permanent(new OrcishBloodpainter());
        bloodpainter.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(bloodpainter);
        return bloodpainter;
    }
}
