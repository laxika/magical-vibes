package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialKavu.class, Forest.class, Island.class, GrizzlyBears.class})
class TerritorialKavuTest extends BaseCardTest {

    private static final String DISCARD_MODE = "Discard a card. If you do, draw a card.";
    private static final String EXILE_MODE = "Exile up to one target card from a graveyard.";

    @Test
    void powerAndToughnessEqualDomainCount() {
        Permanent kavu = addReadyKavu(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    void attackingCanDiscardThenDraw() {
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(drawn));
        addReadyKavu(player1);

        harness.addToBattlefield(player1, new Forest());
        declareAttackers(List.of(0));
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void attackingCanExileUpToOneCardFromAnyGraveyard() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(card));
        addReadyKavu(player1);

        harness.addToBattlefield(player1, new Forest());
        declareAttackers(List.of(0));
        harness.handleListChoice(player1, EXILE_MODE);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).contains(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(card);
    }

    @Test
    void discardAndDrawFinishDuringTheSameResolution() {
        Card discarded = new GrizzlyBears();
        Card drawn = new Island();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(drawn));
        addReadyKavu(player1);
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void emptyHandDoesNotDraw() {
        Card drawn = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        addReadyKavu(player1);
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exileModeCanChooseNoTargetEvenWithCardsAvailable() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));
        addReadyKavu(player1);
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void domainCountsDistinctTypesOnlyOnControlledLandsAndUpdates() {
        Permanent kavu = addReadyKavu(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);

        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(island);
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    void exileModeCanExileALandFromOwnGraveyard() {
        Card card = new Island();
        harness.setGraveyard(player1, List.of(card));
        addReadyKavu(player1);
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
    }

    @Test
    void exileModeIsAvailableWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        addReadyKavu(player1);
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, EXILE_MODE);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiGraveyardChoice) {
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exileDoesNothingIfTargetLeavesGraveyardBeforeResolution() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));
        addReadyKavu(player1);
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(card));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void domainDefinesPowerAndToughnessInHandAndGraveyard() {
        Card inHand = new TerritorialKavu();
        Card inGraveyard = new TerritorialKavu();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(2);
    }

    @Test
    void kavuDiesWithNoBasicLandTypes() {
        harness.addToBattlefield(player1, new TerritorialKavu());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Territorial Kavu");
        harness.assertInGraveyard(player1, "Territorial Kavu");
    }

    private Permanent addReadyKavu(Player player) {
        return addCreatureReady(player, new TerritorialKavu());
    }
}
