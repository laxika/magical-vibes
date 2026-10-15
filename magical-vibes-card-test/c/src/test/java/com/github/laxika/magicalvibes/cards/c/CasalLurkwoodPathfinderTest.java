package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcanisTheOmnipotent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CasalLurkwoodPathfinder.class, CasalPathbreakerOwlbear.class, Forest.class, GrizzlyBears.class,
        ArcanisTheOmnipotent.class})
class CasalLurkwoodPathfinderTest extends BaseCardTest {

    @Test
    void entersAndSearchesForAForestTapped() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.castFromHand(player1, new CasalLurkwoodPathfinder(), "{3}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void attackingMayPayToTransformAndBuffOtherLegendaryCreatures() {
        Permanent casal = addCreatureReady(player1, new CasalLurkwoodPathfinder());
        Permanent otherLegendary = addCreatureReady(player1, new ArcanisTheOmnipotent());
        Permanent nonLegendary = addCreatureReady(player1, new GrizzlyBears());
        int otherLegendaryPower = gqs.getEffectivePower(gd, otherLegendary);
        int otherLegendaryToughness = gqs.getEffectiveToughness(gd, otherLegendary);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(casal.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, casal)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, otherLegendary)).isEqualTo(otherLegendaryPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, otherLegendary)).isEqualTo(otherLegendaryToughness + 2);
        assertThat(gqs.hasKeyword(gd, otherLegendary, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonLegendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonLegendary, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void backFaceTransformsAtItsControllersUpkeep() {
        Permanent casal = addCreatureReady(player1, new CasalLurkwoodPathfinder());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(casal.isTransformed()).isFalse();
        assertThat(casal.getCard().getName()).isEqualTo("Casal, Lurkwood Pathfinder");
    }

    @Test
    void decliningAttackPaymentLeavesCasalOnHerFrontFace() {
        Permanent casal = addCreatureReady(player1, new CasalLurkwoodPathfinder());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(casal.isTransformed()).isFalse();
        assertThat(casal.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, casal, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void enteringWithNoForestStillCompletesTheSearch() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new CasalLurkwoodPathfinder(), "{3}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Casal, Lurkwood Pathfinder");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void transformBuffAffectsOnlyExistingOtherLegendsAndExpiresAtEndOfTurn() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        Permanent casal = addCreatureReady(player1, new CasalLurkwoodPathfinder());
        Permanent ownLegend = addCreatureReady(player1, new ArcanisTheOmnipotent());
        Permanent opposingLegend = addCreatureReady(player2, new ArcanisTheOmnipotent());
        int ownPower = gqs.getEffectivePower(gd, ownLegend);
        int ownToughness = gqs.getEffectiveToughness(gd, ownLegend);
        int opposingPower = gqs.getEffectivePower(gd, opposingLegend);
        int opposingToughness = gqs.getEffectiveToughness(gd, opposingLegend);
        int frontPower = gqs.getEffectivePower(gd, casal);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(casal.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownLegend)).isEqualTo(ownPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, ownLegend)).isEqualTo(ownToughness + 2);
        assertThat(gqs.hasKeyword(gd, ownLegend, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingLegend)).isEqualTo(opposingPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingLegend)).isEqualTo(opposingToughness);
        assertThat(gqs.hasKeyword(gd, opposingLegend, Keyword.TRAMPLE)).isFalse();

        Permanent lateLegend = addCreatureReady(player1, new CasalLurkwoodPathfinder());
        assertThat(gqs.getEffectivePower(gd, lateLegend)).isEqualTo(frontPower);
        assertThat(gqs.hasKeyword(gd, lateLegend, Keyword.TRAMPLE)).isFalse();

        gs.declareBlockers(gd, player2, java.util.List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(casal.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownLegend)).isEqualTo(ownPower);
        assertThat(gqs.getEffectiveToughness(gd, ownLegend)).isEqualTo(ownToughness);
        assertThat(gqs.hasKeyword(gd, ownLegend, Keyword.TRAMPLE)).isFalse();
    }
}
