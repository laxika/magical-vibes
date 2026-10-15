package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaemogothTitan.class, SpinedKarok.class})
class DaemogothTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking forces its controller to sacrifice a creature")
    void attackingSacrificesCreature() {
        Permanent titan = addReadyTitan(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(titan.getId(), bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Daemogoth Titan");
        harness.assertNotOnBattlefield(player1, "Spined Karok");
    }

    @Test
    @DisplayName("Blocking forces its controller to sacrifice a creature")
    void blockingSacrificesCreature() {
        Permanent attacker = addReadyCreature(player1, new SpinedKarok());
        attacker.setAttacking(true);
        addReadyTitan(player2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                gd.playerBattlefields.get(player2.getId()).get(0).getId(), bears.getId());

        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertOnBattlefield(player2, "Daemogoth Titan");
        harness.assertNotOnBattlefield(player2, "Spined Karok");
    }

    @Test
    @DisplayName("The source creature can be sacrificed")
    void sourceCanBeSacrificed() {
        addReadyTitan(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Daemogoth Titan");
    }

    @Test
    @DisplayName("A blocking Titan must sacrifice itself when it is the only creature")
    void loneBlockingTitanSacrificesItself() {
        Permanent attacker = addReadyCreature(player1, new SpinedKarok());
        attacker.setAttacking(true);
        addReadyTitan(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Daemogoth Titan");
        harness.assertInGraveyard(player2, "Daemogoth Titan");
        harness.assertOnBattlefield(player1, "Spined Karok");
    }

    @Test
    @DisplayName("The attack trigger still requires a sacrifice after Titan leaves the battlefield")
    void attackTriggerResolvesWithoutSource() {
        Permanent titan = addReadyTitan(player1);
        harness.addToBattlefield(player1, new SpinedKarok());
        harness.addToBattlefield(player2, new SpinedKarok());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        harness.assertOnBattlefield(player1, "Daemogoth Titan");
        harness.assertOnBattlefield(player1, "Spined Karok");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, titan));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spined Karok");
        harness.assertInGraveyard(player1, "Spined Karok");
        harness.assertOnBattlefield(player2, "Spined Karok");
    }

    private Permanent addReadyTitan(com.github.laxika.magicalvibes.model.Player player) {
        return addReadyCreature(player, new DaemogothTitan());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
