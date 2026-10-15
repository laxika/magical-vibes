package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalcolmTheEyes.class, Shock.class})
class MalcolmTheEyesTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell each turn creates a Clue")
    void secondSpellCreatesClue() {
        addCreatureReady(player1, new MalcolmTheEyes());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Opponent spells do not count toward your second spell")
    void opponentSpellsDoNotCount() {
        addCreatureReady(player1, new MalcolmTheEyes());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Malcolm's own casting counts toward the second spell")
    void malcolmAsFirstSpellCounts() {
        harness.setHand(player1, List.of(new MalcolmTheEyes(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Malcolm cast as the second spell does not trigger on itself or the third spell")
    void malcolmAsSecondSpellDoesNotTrigger() {
        harness.setHand(player1, List.of(new Shock(), new MalcolmTheEyes(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Spell count resets and Malcolm can investigate on an opponent's turn")
    void secondSpellOnNextTurnCreatesAnotherClue() {
        addCreatureReady(player1, new MalcolmTheEyes());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("The created Clue can be sacrificed for two mana to draw a card")
    void clueDrawsCard() {
        addCreatureReady(player1, new MalcolmTheEyes());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));
        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Shock");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("An investigation trigger resolves after Malcolm leaves the battlefield")
    void triggerSurvivesMalcolmRemoval() {
        var malcolm = addCreatureReady(player1, new MalcolmTheEyes());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        harness.castInstant(player2, 0, malcolm.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Malcolm, the Eyes");
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }
}
