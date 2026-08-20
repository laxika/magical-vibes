package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaemogothTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking forces its controller to sacrifice a creature")
    void attackingSacrificesCreature() {
        Permanent titan = addReadyTitan(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(titan.getId(), bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Daemogoth Titan");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blocking forces its controller to sacrifice a creature")
    void blockingSacrificesCreature() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReadyTitan(player2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                gd.playerBattlefields.get(player2.getId()).get(0).getId(), bears.getId());

        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertOnBattlefield(player2, "Daemogoth Titan");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The source creature can be sacrificed")
    void sourceCanBeSacrificed() {
        Permanent titan = addReadyTitan(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, titan.getId());

        harness.assertNotOnBattlefield(player1, "Daemogoth Titan");
    }

    private Permanent addReadyTitan(com.github.laxika.magicalvibes.model.Player player) {
        return addReadyCreature(player, new DaemogothTitan());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
