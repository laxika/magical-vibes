package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RetributiveWandTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability deals 1 damage to a target player")
    void activatedAbilityDealsDamageToPlayer() {
        Permanent wand = harness.addToBattlefieldAndReturn(player1, new RetributiveWand());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(wand.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Activated ability deals 1 damage to a target creature")
    void activatedAbilityDealsDamageToCreature() {
        harness.addToBattlefield(player1, new RetributiveWand());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("When put into a graveyard from the battlefield, deals 5 damage to a target player")
    void graveyardTriggerDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new RetributiveWand());
        harness.setLife(player2, 20);
        destroyWandWithNaturalize();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("When put into a graveyard from the battlefield, deals 5 damage to a target creature")
    void graveyardTriggerDealsDamageToCreature() {
        harness.addToBattlefield(player1, new RetributiveWand());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyWandWithNaturalize();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void destroyWandWithNaturalize() {
        UUID wandId = harness.getPermanentId(player1, "Retributive Wand");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, wandId);
        harness.passBothPriorities();
    }
}
