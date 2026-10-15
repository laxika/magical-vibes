package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuneriderOutlaw;
import com.github.laxika.magicalvibes.cards.g.GaeasAnthem;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CradleToGrave.class, DuneriderOutlaw.class, GaeasAnthem.class, SerraSphinx.class})
class CradleToGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature that entered the battlefield this turn")
    void destroysCreatureThatEnteredThisTurn() {
        Card creature = new SerraSphinx();
        addCreatureToBattlefieldThisTurn(creature);

        castCradleToGrave(creature);

        harness.assertNotOnBattlefield(player2, "Serra Sphinx");
        harness.assertInGraveyard(player2, "Serra Sphinx");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Card creature = new DuneriderOutlaw();
        addCreatureToBattlefieldThisTurn(creature);

        assertThatThrownBy(() -> castCradleToGrave(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a creature that did not enter the battlefield this turn")
    void cannotTargetOlderCreature() {
        Card creature = new SerraSphinx();
        harness.addToBattlefield(player2, creature);

        assertThatThrownBy(() -> castCradleToGrave(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered the battlefield this turn");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent that entered the battlefield this turn")
    void cannotTargetNoncreaturePermanent() {
        Card noncreature = new GaeasAnthem();
        harness.enterBattlefieldAndReturn(player2, noncreature);

        assertThatThrownBy(() -> castCradleToGrave(noncreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Destroys a creature that entered by resolving its spell")
    void destroysCreatureAfterItsSpellResolves() {
        Card creature = new SerraSphinx();
        harness.castFromHand(player1, creature, "{3}{U}{U}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new CradleToGrave()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, creature.getName()));

        harness.assertNotOnBattlefield(player1, "Serra Sphinx");
        harness.assertInGraveyard(player1, "Serra Sphinx");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the caster")
    void destroysOwnCreatureThatEnteredThisTurn() {
        Card creature = new SerraSphinx();
        harness.enterBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new CradleToGrave()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, creature.getName()));

        harness.assertNotOnBattlefield(player1, "Serra Sphinx");
        harness.assertInGraveyard(player1, "Serra Sphinx");
    }

    @Test
    @DisplayName("A creature that entered this turn is no longer eligible on the next turn")
    void cannotTargetCreatureAfterTurnChanges() {
        Card creature = new SerraSphinx();
        harness.enterBattlefieldAndReturn(player2, creature);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> castCradleToGrave(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered the battlefield this turn");
        harness.assertOnBattlefield(player2, "Serra Sphinx");
        harness.assertNotInGraveyard(player2, "Serra Sphinx");
    }

    private void addCreatureToBattlefieldThisTurn(Card creature) {
        harness.enterBattlefieldAndReturn(player2, creature);
    }

    private void castCradleToGrave(Card creature) {
        harness.setHand(player1, List.of(new CradleToGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, creature.getName()));
    }
}
