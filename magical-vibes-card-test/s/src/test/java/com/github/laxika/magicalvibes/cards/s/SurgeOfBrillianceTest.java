package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgeOfBrilliance.class, GrizzlyBears.class})
class SurgeOfBrillianceTest extends BaseCardTest {

    @Test
    void drawsNothingWithoutOutsideHandCasts() {
        Card libraryCard = new SurgeOfBrilliance();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.castFromHand(player1, new SurgeOfBrilliance(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void countsOutsideHandSpellsCastInResponseAtResolution() {
        Card firstDraw = new SurgeOfBrilliance();
        Card secondDraw = new SurgeOfBrilliance();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.castFromHand(player1, new SurgeOfBrilliance(), "{1}{U}");

        SurgeOfBrilliance response = new SurgeOfBrilliance();
        gd.addToExile(player1.getId(), response);
        gd.exilePlayPermissions.put(response.getId(), player1.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, response.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void doesNotCountOpponentsOutsideHandCasts() {
        Card ownLibraryCard = new SurgeOfBrilliance();
        Card opponentDraw = new SurgeOfBrilliance();
        harness.setLibrary(player1, List.of(ownLibraryCard));
        harness.setLibrary(player2, List.of(opponentDraw));
        SurgeOfBrilliance opponentSpell = new SurgeOfBrilliance();
        gd.addToExile(player2.getId(), opponentSpell);
        gd.exilePlayPermissions.put(opponentSpell.getId(), player2.getId());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, opponentSpell.getId());
        harness.passBothPriorities();

        harness.castFromHand(player1, new SurgeOfBrilliance(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownLibraryCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentDraw);
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        SurgeOfBrilliance spell = new SurgeOfBrilliance();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsForEachOutsideHandCastIncludingUnresolvedSpells() {
        Card firstDraw = new SurgeOfBrilliance();
        Card secondDraw = new SurgeOfBrilliance();
        Card remainingCard = new SurgeOfBrilliance();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, remainingCard));
        for (int i = 0; i < 2; i++) {
            SurgeOfBrilliance spell = new SurgeOfBrilliance();
            gd.addToExile(player1.getId(), spell);
            gd.exilePlayPermissions.put(spell.getId(), player1.getId());
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castFromExile(player1, spell.getId());
        }

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Draws for spells cast from outside the hand, but not hand casts")
    void drawsForOutsideHandSpellsOnly() {
        Card exiledSpell = new GrizzlyBears();
        Card handSpell = new GrizzlyBears();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        gd.addToExile(player1.getId(), exiledSpell);
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, exiledSpell.getId());
        harness.passBothPriorities();

        harness.castFromHand(player1, handSpell, "{1}{G}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new SurgeOfBrilliance(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Can be foretold and draws for its own non-hand cast")
    void foretellsAndCastsFromExile() {
        Card drawnCard = new GrizzlyBears();
        SurgeOfBrilliance spell = new SurgeOfBrilliance();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
