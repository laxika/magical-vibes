package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarahJaneSmith.class, Spellbook.class, GrizzlyBears.class})
class SarahJaneSmithTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a historic spell investigates")
    void historicSpellCreatesAClue() {
        harness.addToBattlefield(player1, new SarahJaneSmith());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

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

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
