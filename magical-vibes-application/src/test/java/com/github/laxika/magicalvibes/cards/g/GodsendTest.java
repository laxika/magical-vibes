package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GuardianOfTheGateless;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GodsendTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsPlusThreePlusThree() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent godsend = addGodsend(player1);
        godsend.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates one may trigger")
    void becomesBlockedExilesChosenBlocker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent godsend = addGodsend(player1);
        godsend.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).filteredOn(se -> se.getCard().getName().equals("Godsend")).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstBlocker.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondBlocker)
                .doesNotContain(firstBlocker);
        assertThat(gd.getCardsExiledByPermanent(godsend.getId()))
                .containsExactly(firstBlocker.getOriginalCard());
    }

    @Test
    @DisplayName("A Godsend block trigger fires once when one creature blocks multiple attackers")
    void blocksMultipleCreaturesExileOneChosenAttacker() {
        Permanent blocker = addCreatureReady(player2, new GuardianOfTheGateless());
        Permanent godsend = addGodsend(player2);
        godsend.setAttachedTo(blocker.getId());

        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));

        assertThat(gd.stack).filteredOn(se -> se.getCard().getName().equals("Godsend")).hasSize(1);
    }

    @Test
    @DisplayName("Declining Godsend's may ability leaves combat creatures in place")
    void decliningMayAbilityDoesNotExile() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent godsend = addGodsend(player1);
        godsend.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.getCardsExiledByPermanent(godsend.getId())).isEmpty();
    }

    private Permanent addGodsend(Player player) {
        Permanent perm = new Permanent(new Godsend());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
