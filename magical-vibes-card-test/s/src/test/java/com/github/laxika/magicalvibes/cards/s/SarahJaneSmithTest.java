package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnUnearthlyChild;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RoseTyler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarahJaneSmith.class, Spellbook.class, GrizzlyBears.class, RoseTyler.class, AnUnearthlyChild.class})
class SarahJaneSmithTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a historic spell investigates")
    void historicSpellCreatesAClue() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isOne();
    }

    @Test
    @DisplayName("The ability triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isOne();
    }

    @Test
    @DisplayName("Casting a nonhistoric spell does not investigate")
    void nonHistoricSpellDoesNotCreateAClue() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(countPermanents(player1, "Clue")).isZero();
    }

    @Test
    @DisplayName("The ability can trigger again on a later turn")
    void triggersAgainOnLaterTurn() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceTurn();
        advanceTurn();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonartifact legendary spell investigates")
    void legendarySpellCreatesAClue() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.castFromHand(player1, new RoseTyler(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isOne();
        assertThat(countPermanents(player1, "Rose Tyler")).isZero();
    }

    @Test
    @DisplayName("A nonlegendary Saga spell investigates")
    void sagaSpellCreatesAClue() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.castFromHand(player1, new AnUnearthlyChild(), "{1}{U}{U}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isOne();
        assertThat(countPermanents(player1, "An Unearthly Child")).isZero();
    }

    @Test
    @DisplayName("A nonhistoric spell does not consume the once-per-turn trigger")
    void nonHistoricSpellDoesNotPreventLaterTrigger() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isOne();
    }

    @Test
    @DisplayName("An opponent's historic spell does not investigate")
    void opponentHistoricSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Spellbook(), "{0}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isZero();
        assertThat(countPermanents(player2, "Clue")).isZero();
    }

    @Test
    @DisplayName("Casting Sarah Jane Smith does not trigger her own ability")
    void doesNotInvestigateForHerOwnCast() {
        harness.castFromHand(player1, new SarahJaneSmith(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sarah Jane Smith")).isOne();
        assertThat(countPermanents(player1, "Clue")).isZero();
    }

    @Test
    @DisplayName("An investigated Clue can be sacrificed for two mana to draw a card")
    void clueCanBeSacrificedToDraw() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.setLibrary(player1, List.of(new RoseTyler()));
        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);

        assertThat(countPermanents(player1, "Clue")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
