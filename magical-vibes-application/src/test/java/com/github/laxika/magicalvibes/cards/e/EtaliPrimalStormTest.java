package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtaliPrimalStorm.class, Forest.class, GrizzlyBears.class, Shock.class, VillageRites.class, RuleOfLaw.class})
class EtaliPrimalStormTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card of each library and offers every exiled spell")
    void exilesTopCardOfEachLibraryAndOffersSpells() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Forest player1Remainder = new Forest();
        Forest player2Remainder = new Forest();
        harness.setLibrary(player1, List.of(shock, player1Remainder));
        harness.setLibrary(player2, List.of(bears, player2Remainder));
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactlyInAnyOrder(shock.getId(), bears.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(shock, bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Remainder);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Remainder);
    }

    @Test
    @DisplayName("Casts an opponent-owned exiled spell without paying and leaves unchosen cards exiled")
    void castsOpponentOwnedSpellWithoutPaying() {
        Forest land = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(bears));
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.stack).anyMatch(stackEntry -> stackEntry.getCard() == bears
                && stackEntry.getControllerId().equals(player1.getId()));
        assertThat(gd.stack.stream().map(stackEntry -> stackEntry.getCard().getId()))
                .doesNotContain(land.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).contains(land);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).doesNotContain(bears);
    }

    @Test
    void mayDeclineAllSpellsAndTheyRemainExiled() {
        GrizzlyBears ownSpell = new GrizzlyBears();
        GrizzlyBears opposingSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownSpell));
        harness.setLibrary(player2, List.of(opposingSpell));
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(ownSpell, opposingSpell);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == ownSpell
                || entry.getCard() == opposingSpell);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotPreventExilingOtherPlayersCard() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(spell));
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).containsExactly(spell.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(spell);
    }

    @Test
    void castsBothSpellsInChosenOrderWithoutResolvingBetweenCasts() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first));
        harness.setLibrary(player2, List.of(second));
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), first.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard()).containsExactly(second, first);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void offersPaymentOfMandatoryAdditionalCostWhenCreatureIsAvailable() {
        VillageRites spell = new VillageRites();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));

        // Etali itself can be sacrificed to pay the additional cost.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void cannotCastSecondExiledSpellUnderRuleOfLaw() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first));
        harness.setLibrary(player2, List.of(second));
        harness.addToBattlefield(player2, new RuleOfLaw());
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard()).containsExactly(first);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).contains(second);
    }

    @Test
    void allLandCardsRemainExiledWithoutOfferingCastChoice() {
        Forest ownLand = new Forest();
        Forest opposingLand = new Forest();
        harness.setLibrary(player1, List.of(ownLand));
        harness.setLibrary(player2, List.of(opposingLand));
        addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(ownLand, opposingLand);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == ownLand
                || entry.getCard() == opposingLand);
    }

    @Test
    void opponentOwnedInstantCanTargetEtaliAndReturnsToOwnersGraveyard() {
        Shock spell = new Shock();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(spell));
        var etali = addCreatureReady(player1, new EtaliPrimalStorm());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.handlePermanentChosen(player1, etali.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == spell
                && entry.getControllerId().equals(player1.getId())
                && entry.getTargetId().equals(etali.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }
}
