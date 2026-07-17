package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AurochsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new Aurochs());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Aurochs"));
    }

    @Test
    @DisplayName("Gets +0/+0 when attacking alone")
    void noBoostWhenAttackingAlone() {
        Permanent aurochs = addCreatureReady(player1, new Aurochs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(aurochs.getPowerModifier()).isEqualTo(0);
        assertThat(aurochs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+0 when attacking with one other Aurochs")
    void boostWithOneOtherAurochs() {
        Permanent aurochs = addCreatureReady(player1, new Aurochs());
        addCreatureReady(player1, new Aurochs());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(aurochs.getPowerModifier()).isEqualTo(1);
        assertThat(aurochs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +2/+0 when attacking with two other Aurochs")
    void boostWithTwoOtherAurochs() {
        Permanent aurochs = addCreatureReady(player1, new Aurochs());
        addCreatureReady(player1, new Aurochs());
        addCreatureReady(player1, new Aurochs());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(aurochs.getPowerModifier()).isEqualTo(2);
        assertThat(aurochs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-Aurochs attackers do not count")
    void nonAurochsAttackersDoNotCount() {
        Permanent aurochs = addCreatureReady(player1, new Aurochs());
        addCreatureReady(player1, createNonAurochsCard("Goblin Grunt"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(aurochs.getPowerModifier()).isEqualTo(0);
        assertThat(aurochs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only counts attacking Aurochs, not non-attacking ones")
    void onlyCountsAttackingAurochs() {
        Permanent aurochs = addCreatureReady(player1, new Aurochs());
        addCreatureReady(player1, new Aurochs());
        addCreatureReady(player1, new Aurochs());

        // Only index 0 and 1 attack; index 2 stays home.
        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(aurochs.getPowerModifier()).isEqualTo(1);
        assertThat(aurochs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent aurochs = addCreatureReady(player1, new Aurochs());
        addCreatureReady(player1, new Aurochs());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(aurochs.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aurochs.getPowerModifier()).isEqualTo(0);
        assertThat(aurochs.getToughnessModifier()).isEqualTo(0);
    }

    private Card createNonAurochsCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.GOBLIN));
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
