package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VibranceTest extends BaseCardTest {

    @Test
    void twoRedManaDealsThreeDamageToAnyTarget() {
        harness.setHand(player1, List.of(new Vibrance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Vibrance");
    }

    @Test
    void twoGreenManaSearchesForLandAndGainsTwoLife() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Plains()));
        harness.setHand(player1, List.of(new Vibrance()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 22);
        harness.assertOnBattlefield(player1, "Vibrance");
    }

    @Test
    void oneManaOfEachColorTriggersNeitherBranch() {
        harness.setHand(player1, List.of(new Vibrance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Vibrance");
    }

    @Test
    void evokeWithTwoGreenManaSearchesAndSacrificesVibrance() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Vibrance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player1, "Vibrance");
        harness.assertInGraveyard(player1, "Vibrance");
    }

    @Test
    void redBranchCannotTargetALand() {
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Plains()).getId();
        harness.setHand(player1, List.of(new Vibrance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(landId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
