package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkroanHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 for the number of attacking creatures you control")
    void boostsForAttackingCreaturesYouControl() {
        Permanent hoplite = addCreatureReady(player1, new AkroanHoplite());
        addCreatureReady(player1, createCreatureCard("Ally"));
        addCreatureReady(player2, createCreatureCard("Opponent"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(hoplite.getPowerModifier()).isEqualTo(2);
        assertThat(hoplite.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent hoplite = addCreatureReady(player1, new AkroanHoplite());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(hoplite.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hoplite.getPowerModifier()).isZero();
    }

    private com.github.laxika.magicalvibes.model.Card createCreatureCard(String name) {
        com.github.laxika.magicalvibes.model.Card card = new com.github.laxika.magicalvibes.model.Card() {};
        card.setName(name);
        card.setType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
