package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErdwalIlluminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Investigating for the first time each turn creates an additional Clue")
    void firstInvestigationCreatesAdditionalClue() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.setHand(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger after the first investigation even if it entered later")
    void doesNotTriggerAfterFirstInvestigation() {
        harness.setHand(player1, List.of(new ThrabenInspector(), new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }
}
