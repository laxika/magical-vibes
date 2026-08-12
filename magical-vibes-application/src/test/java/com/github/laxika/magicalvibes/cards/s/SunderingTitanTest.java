package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunderingTitanTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets its controller choose a land from any battlefield and destroys it")
    void etbControllerChoosesLandFromAnyBattlefield() {
        Permanent playerOneMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent playerTwoMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.setHand(player1, List.of(new SunderingTitan()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                playerOneMountain.getId(), playerTwoMountain.getId());
        assertThat(choice.context()).isInstanceOf(
                MultiPermanentChoiceContext.ChooseLandOfEachBasicTypeThenDestroyChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(playerTwoMountain.getId()));

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Sundering Titan");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(playerTwoMountain.getCard());
    }

    @Test
    @DisplayName("When it leaves, its controller chooses lands and the selected lands are destroyed")
    void leavesBattlefieldTriggersTheSameEffect() {
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new SunderingTitan());
        Permanent playerOneMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent playerTwoMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, titan.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                playerOneMountain.getId(), playerTwoMountain.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(playerOneMountain.getId()));

        harness.assertNotOnBattlefield(player1, "Sundering Titan");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player1, "Sundering Titan");
    }
}
