package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.e.EsperPanorama;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindlockOrb.class, DiabolicTutor.class, GrizzlyBears.class, Plains.class,
        Swamp.class, EsperPanorama.class, SongOfTheDryads.class})
class MindlockOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot search their library even with plenty of mana (no pay-to-ignore)")
    void opponentCannotSearchEvenWithMana() {
        harness.addToBattlefield(player1, new MindlockOrb());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 8); // 4 for Tutor, plenty extra — cannot pay it away

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("prevented by Mindlock Orb"));
    }

    @Test
    @DisplayName("Controller also cannot search their own library")
    void controllerCannotSearch() {
        harness.addToBattlefield(player1, new MindlockOrb());

        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("prevented by Mindlock Orb"));
    }

    @Test
    @DisplayName("Library is still shuffled when the search is prevented")
    void libraryShuffledWhenPrevented() {
        harness.addToBattlefield(player1, new MindlockOrb());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);
        List<Card> deck = gd.playerDecks.get(player2.getId());
        int deckSizeBefore = deck.size();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(deck).hasSize(deckSizeBefore); // preserved, not emptied
    }

    @Test
    @DisplayName("There is no search tax to pay — paying is rejected")
    void payingSearchTaxRejected() {
        harness.addToBattlefield(player1, new MindlockOrb());

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 8);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);

        assertThatThrownBy(() -> harness.paySearchTax(player2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Mindlock Orb restores the ability to search")
    void removingOrbRestoresSearch() {
        harness.addToBattlefield(player1, new MindlockOrb());
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Mindlock Orb"));

        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setupLibrary(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("An activated search is prevented but its sacrifice cost is still paid")
    void activatedSearchStillPaysCosts() {
        harness.addToBattlefield(player1, new MindlockOrb());
        harness.addToBattlefield(player2, new EsperPanorama());
        harness.setLibrary(player2, List.of(new Plains(), new Swamp()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.assertInGraveyard(player2, "Esper Panorama");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertNotOnBattlefield(player2, "Swamp");
    }

    @Test
    @DisplayName("A remaining Orb still prevents searches after another Orb leaves")
    void remainingOrbStillPreventsSearch() {
        Permanent firstOrb = harness.addToBattlefieldAndReturn(player1, new MindlockOrb());
        harness.addToBattlefield(player2, new MindlockOrb());
        gd.playerBattlefields.get(player1.getId()).remove(firstOrb);
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        setupLibrary(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Orb turned into a Forest by Song of the Dryads no longer prevents searches")
    void orbWithPrintedAbilitiesRemovedAllowsSearch() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new MindlockOrb());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SongOfTheDryads(), new DiabolicTutor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);
        setupLibrary(player1);

        harness.castEnchantment(player1, 0, orb.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    private void setupLibrary(Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
