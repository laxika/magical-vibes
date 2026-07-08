package com.github.laxika.magicalvibes.cards.c;

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

class CennsHeirTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new CennsHeir());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Cenn's Heir"));
    }

    @Test
    @DisplayName("Gets +0/+0 when attacking alone (no other Kithkin)")
    void noBoostWhenAttackingAlone() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(0);
        assertThat(heir.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+1 when attacking with one other Kithkin")
    void boostWithOneOtherKithkin() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, createKithkinCard("Test Kithkin"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(1);
        assertThat(heir.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 when attacking with two other Kithkin")
    void boostWithTwoOtherKithkin() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, createKithkinCard("Kithkin A"));
        addCreatureReady(player1, createKithkinCard("Kithkin B"));

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(2);
        assertThat(heir.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Kithkin attackers do not count")
    void nonKithkinAttackersDoNotCount() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, createNonKithkinCard("Human Soldier"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(0);
        assertThat(heir.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only counts attacking Kithkin, not non-attacking ones")
    void onlyCountsAttackingKithkin() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, createKithkinCard("Attacking Kithkin"));
        addCreatureReady(player1, createKithkinCard("Staying Home Kithkin"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(1);
        assertThat(heir.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, createKithkinCard("Test Kithkin"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(heir.getPowerModifier()).isEqualTo(0);
        assertThat(heir.getToughnessModifier()).isEqualTo(0);
    }

    private Card createKithkinCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.KITHKIN));
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createNonKithkinCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER));
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
