package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PhyrexianLens;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dredge.class, KavuTitan.class, Mountain.class, PhyrexianLens.class})
class DredgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature on resolution lets Dredge draw a card")
    void sacrificesCreatureAndDrawsCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KavuTitan());
        KavuTitan drawnCard = new KavuTitan();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new Dredge(), "{B}");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kavu Titan");
        harness.assertInGraveyard(player1, "Kavu Titan");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Sacrificing a land on resolution lets Dredge draw a card")
    void sacrificesLandAndDrawsCard() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        KavuTitan drawnCard = new KavuTitan();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new Dredge(), "{B}");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("With no creature or land, Dredge still draws a card")
    void drawsWithoutMatchingPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PhyrexianLens());
        KavuTitan drawnCard = new KavuTitan();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new Dredge(), "{B}");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        harness.assertNotInGraveyard(player1, "Phyrexian Lens");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("With multiple eligible permanents, Dredge chooses one on resolution")
    void choosesOnePermanentOnResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KavuTitan());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        KavuTitan drawnCard = new KavuTitan();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new Dredge(), "{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(land);
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }
}
