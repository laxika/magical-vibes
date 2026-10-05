package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GaeasCradle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaraCroftTombRaider.class, GaeasCradle.class, MoxAmber.class, GrizzlyBears.class})
class LaraCroftTombRaiderTest extends BaseCardTest {

    @Test
    void attackingExilesOptionalLegendaryArtifactOrLandWithDiscoveryCounter() {
        addCreatureReady(player1, new LaraCroftTombRaider());
        Card validLand = new GaeasCradle();
        Card validArtifact = new MoxAmber();
        Card invalid = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(invalid)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(validLand, validArtifact)));

        declareAttackers(List.of(0));

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(validLand.getId(), validArtifact.getId());
        assertThat(choice.validCardIds()).doesNotContain(invalid.getId());

        harness.handleMultipleCardsChosen(player1, List.of(validLand.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(validLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(validLand);
        assertThat(gd.exiledCardsWithDiscoveryCounters).contains(validLand.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(validLand.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(validLand.getId());
    }

    @Test
    void raidCreatesTreasureAtEndOfCombat() {
        addCreatureReady(player1, new LaraCroftTombRaider());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void attackWithoutGraveyardTargetsAllowsPlayingPreviouslyDiscoveredCard() {
        addCreatureReady(player1, new LaraCroftTombRaider());
        Card discovered = new MoxAmber();
        gd.addToExileWithDiscoveryCounter(player2.getId(), discovered);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.castFromExile(player1, discovered.getId());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Mox Amber")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(discovered);
    }

    @Test
    void decliningExileStillAllowsPlayingPreviouslyDiscoveredCard() {
        addCreatureReady(player1, new LaraCroftTombRaider());
        Card discovered = new MoxAmber();
        Card target = new GaeasCradle();
        gd.addToExileWithDiscoveryCounter(player2.getId(), discovered);
        harness.setGraveyard(player2, List.of(target));

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMultipleCardsChosen(player1, List.of()));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.castFromExile(player1, discovered.getId());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Mox Amber")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
    }

    @Test
    void mayPlayOlderDiscoveryInsteadOfNewlyExiledCardButOnlyOneCard() {
        addCreatureReady(player1, new LaraCroftTombRaider());
        Card discovered = new MoxAmber();
        Card target = new GaeasCradle();
        gd.addToExileWithDiscoveryCounter(player2.getId(), discovered);
        harness.setGraveyard(player2, List.of(target));

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMultipleCardsChosen(player1, List.of(target.getId())));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.castFromExile(player1, discovered.getId());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Mox Amber")).hasSize(1);
        assertThatThrownBy(() -> harness.castFromExile(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    void newlyExiledArtifactCanBeCastDuringPostcombatMain() {
        addCreatureReady(player1, new LaraCroftTombRaider());
        Card target = new MoxAmber();
        harness.setGraveyard(player2, List.of(target));

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMultipleCardsChosen(player1, List.of(target.getId())));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.castFromExile(player1, target.getId());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Mox Amber")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target);
    }

    @Test
    void raidCreatesTreasureWhenAnotherCreatureAttacksAndLaraDoesNot() {
        harness.addToBattlefield(player1, new LaraCroftTombRaider());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void raidDoesNotCreateTreasureWithoutAnAttack() {
        addCreatureReady(player1, new LaraCroftTombRaider());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of()));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
