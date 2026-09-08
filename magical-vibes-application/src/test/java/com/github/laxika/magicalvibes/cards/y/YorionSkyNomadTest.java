package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.n.NullRod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YorionSkyNomad.class, GrizzlyBears.class, Forest.class, NullRod.class, LayClaim.class})
class YorionSkyNomadTest extends BaseCardTest {

    @Test
    void choosesOwnAndControlledNonlandPermanentsAndReturnsThemAtNextEndStep() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new NullRod());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new YorionSkyNomad()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ownCreature.getId(), ownArtifact.getId());
        assertThat(choice.validIds()).doesNotContain(
                ownLand.getId(), opponentCreature.getId(),
                findPermanent(player1, "Yorion, Sky Nomad").getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId(), ownArtifact.getId()));

        harness.assertNotOnBattlefield(player1, "Null Rod");
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        harness.assertOnBattlefield(player1, "Null Rod");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    void mayChooseNoPermanents() {
        harness.setHand(player1, List.of(new YorionSkyNomad()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
