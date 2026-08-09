package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpitefulBullyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target creature you control at your upkeep")
    void dealsDamageToControlledCreature() {
        addCreatureReady(player1, new SpitefulBully());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target Spiteful Bully itself")
    void canTargetItself() {
        Permanent bully = addCreatureReady(player1, new SpitefulBully());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bully.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spiteful Bully");
        harness.assertInGraveyard(player1, "Spiteful Bully");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new SpitefulBully());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
