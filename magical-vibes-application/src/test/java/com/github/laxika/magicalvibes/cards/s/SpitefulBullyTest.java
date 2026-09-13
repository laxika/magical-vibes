package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BelbesArmor;
import com.github.laxika.magicalvibes.cards.b.BelbesPercher;
import com.github.laxika.magicalvibes.cards.f.FlowstoneWall;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulBully.class, BelbesPercher.class, BelbesArmor.class, FlowstoneWall.class})
class SpitefulBullyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target creature you control at your upkeep")
    void dealsDamageToControlledCreature() {
        addCreatureReady(player1, new SpitefulBully());
        Permanent ownCreature = addCreatureReady(player1, new BelbesPercher());
        Permanent opponentCreature = addCreatureReady(player2, new BelbesPercher());
        Permanent ownNonCreature = harness.addToBattlefieldAndReturn(player1, new BelbesArmor());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId(), ownNonCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Belbe's Percher");
        harness.assertInGraveyard(player1, "Belbe's Percher");
    }

    @Test
    @DisplayName("Deals exactly 3 damage to the chosen creature")
    void dealsExactlyThreeDamage() {
        addCreatureReady(player1, new SpitefulBully());
        Permanent target = addCreatureReady(player1, new FlowstoneWall());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Flowstone Wall");
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
