package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlivMizzetHivemind.class, SyphonSliver.class, GrizzlyBears.class})
class SlivMizzetHivemindTest extends BaseCardTest {

    @Test
    @DisplayName("Sliv-Mizzet gives flying to Slivers you control only")
    void grantsFlyingToOwnSliversOnly() {
        Permanent source = addCreatureReady(player1, new SlivMizzetHivemind());
        Permanent ownSliver = addCreatureReady(player1, new SyphonSliver());
        Permanent ownNonSliver = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingSliver = addCreatureReady(player2, new SyphonSliver());

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sliv-Mizzet's tap-to-draw ability triggers its damage ability")
    void drawsAndDealsDamageToPlayer() {
        addCreatureReady(player1, new SlivMizzetHivemind());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Another Sliver you control gets the tap-to-draw and draw-damage abilities")
    void grantsAbilitiesToOtherSlivers() {
        addCreatureReady(player1, new SlivMizzetHivemind());
        addCreatureReady(player1, new SyphonSliver());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Each Sliver deals its own damage to a creature and gains life from lifelink")
    void sliversDealDamageToCreatureWithLifelink() {
        addCreatureReady(player1, new SlivMizzetHivemind());
        addCreatureReady(player1, new SyphonSliver());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A draw triggers only the drawing player's Slivers")
    void opponentDrawDoesNotTriggerOwnSlivers() {
        addCreatureReady(player1, new SlivMizzetHivemind());
        addCreatureReady(player1, new SyphonSliver());
        Permanent opposingSource = addCreatureReady(player2, new SlivMizzetHivemind());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        assertThat(opposingSource.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
