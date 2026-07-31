package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FurnaceWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScourgeOfValkasTest extends BaseCardTest {

    @Test
    @DisplayName("Its own enter trigger deals 1 damage when it is the only Dragon")
    void ownEnterDealsOneDamageAsLoneDragon() {
        harness.setLife(player2, 20);
        castScourge(player1);

        harness.passBothPriorities(); // creature spell resolves, enter trigger goes on the stack
        harness.passBothPriorities(); // enter trigger resolves

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Its own enter trigger counts every Dragon its controller has, including itself")
    void ownEnterCountsAllDragons() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FurnaceWhelp());
        castScourge(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Another Dragon entering queues the damage trigger for target selection")
    void anotherDragonEnterQueuesTargetSelection() {
        harness.addToBattlefield(player1, new ScourgeOfValkas());

        castFurnaceWhelp(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class);
    }

    @Test
    @DisplayName("Another Dragon entering deals damage equal to the number of Dragons controlled")
    void anotherDragonEnterDealsDamageToChosenCreature() {
        harness.addToBattlefield(player1, new ScourgeOfValkas());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFurnaceWhelp(player1);
        harness.passBothPriorities(); // Furnace Whelp enters → trigger awaits a target

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities(); // trigger resolves: 2 Dragons → 2 damage kills the 2/2

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A non-Dragon creature entering does not trigger the damage ability")
    void nonDragonEnterDoesNotTrigger() {
        harness.addToBattlefield(player1, new ScourgeOfValkas());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{R} pumps it by +1/+0 until end of turn")
    void firebreathingPumpsPower() {
        Permanent scourge = harness.addToBattlefieldAndReturn(player1, new ScourgeOfValkas());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(4);
    }

    private void castScourge(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new ScourgeOfValkas()));
        harness.addMana(player, ManaColor.RED, 5);
        harness.getGameService().playCard(harness.getGameData(), player, 0, 0, player2.getId(), null);
    }

    private void castFurnaceWhelp(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new FurnaceWhelp()));
        harness.addMana(player, ManaColor.RED, 5);
        harness.castCreature(player, 0);
    }
}
