package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuicklingTest extends BaseCardTest {

    @Test
    void sacrificesWhenItIsTheOnlyCreatureItsControllerHas() {
        castQuickling();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Quickling");
        harness.assertInGraveyard(player1, "Quickling");
    }

    @Test
    void returningAnotherCreatureKeepsQuickling() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castQuickling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertOnBattlefield(player1, "Quickling");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void decliningTheReturnSacrificesQuickling() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castQuickling();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Quickling");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentCreatureDoesNotSatisfyTheRequirement() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castQuickling();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Quickling");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void quicklingIsNotAValidReturnChoice() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castQuickling();
        harness.handleMayAbilityChosen(player1, true);

        UUID quicklingId = findPermanent(player1, "Quickling").getId();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .doesNotContain(quicklingId);
    }

    private void castQuickling() {
        harness.setHand(player1, List.of(new Quickling()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
