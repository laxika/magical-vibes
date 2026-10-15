package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CunningEvasion.class, GrizzlyBears.class})
class CunningEvasionTest extends BaseCardTest {

    @Test
    @DisplayName("May return a creature you control to its owner's hand when it becomes blocked")
    void acceptsReturnToHand() {
        addReady(player1, new CunningEvasion());
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the blocked creature on the battlefield")
    void declinesReturnToHand() {
        addReady(player1, new CunningEvasion());
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when a creature you control is unblocked")
    void unblockedCreatureDoesNotTrigger() {
        addReady(player1, new CunningEvasion());
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Multiple blockers trigger only once for the blocked creature")
    void multipleBlockersTriggerOnce() {
        addReady(player1, new CunningEvasion());
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Cunning Evasion");
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature becoming blocked")
    void opponentBlockedCreatureDoesNotTrigger() {
        addReady(player1, new CunningEvasion());
        addReady(player1, new GrizzlyBears());
        Permanent attacker = addReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player2, "Grizzly Bears");
    }
    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
