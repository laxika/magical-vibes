package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LesserMasticore;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThroesOfChaos.class, Forest.class, Mountain.class, HillGiant.class, GrizzlyBears.class,
        LesserMasticore.class})
class ThroesOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade casts the first qualifying card for free and puts skipped cards on the bottom")
    void cascadeCastsFirstCheaperNonland() {
        Mountain skippedLand = new Mountain();
        HillGiant skippedNonland = new HillGiant();
        GrizzlyBears hit = new GrizzlyBears();
        Forest belowHit = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, skippedNonland, hit, belowHit));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new ThroesOfChaos(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, skippedLand, skippedNonland);
    }

    @Test
    @DisplayName("Retrace casts Throes of Chaos from the graveyard by discarding a land")
    void retraceCastsFromGraveyard() {
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.setGraveyard(player1, List.of(new ThroesOfChaos(), new Mountain(), new Forest()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Throes of Chaos");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningCascadeBottomsTheHitAndSkippedCards() {
        Mountain skipped = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(skipped, hit, untouched));
        harness.castFromHand(player1, new ThroesOfChaos(), "{3}{R}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skipped, hit);
        assertThat(gd.findExiledCard(skipped.getId())).isNull();
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == hit);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Throes of Chaos");
    }

    @Test
    void retraceTriggersCascadeAndCanBeUsedAgain() {
        ThroesOfChaos spell = new ThroesOfChaos();
        GrizzlyBears hit = new GrizzlyBears();
        Mountain firstLand = new Mountain();
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(hit));
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(firstLand, secondLand));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell, firstLand);
        harness.castRetrace(player1, gd.playerGraveyards.get(player1.getId()).indexOf(spell), 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell, firstLand, secondLand);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    @Test
    void retraceRejectsNonlandDiscardWithoutSpendingMana() {
        ThroesOfChaos spell = new ThroesOfChaos();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(nonland));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void retraceStillRequiresTheNormalManaCost() {
        ThroesOfChaos spell = new ThroesOfChaos();
        Mountain land = new Mountain();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(land));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cascadeMustPayMandatoryAdditionalDiscardCost() {
        LesserMasticore hit = new LesserMasticore();
        ThroesOfChaos discard = new ThroesOfChaos();
        harness.setLibrary(player1, List.of(hit));
        harness.castFromHand(player1, new ThroesOfChaos(), "{3}{R}");
        harness.setHand(player1, List.of(discard));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit);
    }
}
