package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhituSlingerTest extends BaseCardTest {

    @Test
    void entersAndDealsTwoDamageToTargetPlayer() {
        castAndResolveGhituSlinger(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Ghitu Slinger");
    }

    @Test
    void entersAndDealsTwoDamageToTargetCreature() {
        GrizzlyBears card = new GrizzlyBears();
        card.setToughness(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);

        castAndResolveGhituSlinger(creature.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void decliningEchoSacrificesGhituSlingerAtItsNextUpkeep() {
        castAndResolveGhituSlinger(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ghitu Slinger");
        harness.assertInGraveyard(player1, "Ghitu Slinger");
    }

    @Test
    void payingEchoKeepsGhituSlingerAndEchoDoesNotTriggerAgain() {
        castAndResolveGhituSlinger(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Ghitu Slinger");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Ghitu Slinger");
    }

    private void castAndResolveGhituSlinger(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new GhituSlinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
