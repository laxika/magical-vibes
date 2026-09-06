package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DirgeBat.class, GrizzlyBears.class, JaceBeleren.class})
class DirgeBatTest extends BaseCardTest {

    @Test
    void mutatingDestroysTargetCreatureAnOpponentControls() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        triggerMutation(bat);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void mutatingDestroysTargetPlaneswalkerAnOpponentControls() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent jace = addReadyJace(player2);

        triggerMutation(bat);
        harness.handlePermanentChosen(player1, jace.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        harness.assertInGraveyard(player2, "Jace Beleren");
    }

    @Test
    void mutatingCannotTargetOwnCreature() {
        Permanent bat = addCreatureReady(player1, new DirgeBat());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        triggerMutation(bat);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private void triggerMutation(Permanent bat) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, bat, List.of(bat.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }

    private Permanent addReadyJace(Player player) {
        Permanent jace = new Permanent(new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        jace.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jace);
        return jace;
    }
}
