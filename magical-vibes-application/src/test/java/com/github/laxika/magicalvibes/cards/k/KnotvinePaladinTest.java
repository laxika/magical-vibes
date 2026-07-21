package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
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

class KnotvinePaladinTest extends BaseCardTest {

    // ===== Attack trigger fires =====

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new KnotvinePaladin());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Knotvine Paladin"));
    }

    // ===== Boost based on untapped creatures you control =====

    @Test
    @DisplayName("Gets +0/+0 when attacking with no other creatures (it taps itself)")
    void noBoostWhenAttackingAlone() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(paladin.getPowerModifier()).isEqualTo(0);
        assertThat(paladin.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +N/+N for each untapped creature staying back")
    void boostForUntappedCreatures() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        addCreatureReady(player1, createCreatureCard("Untapped A"));
        addCreatureReady(player1, createCreatureCard("Untapped B"));

        // Only the Paladin attacks; the other two stay back untapped.
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(paladin.getPowerModifier()).isEqualTo(2);
        assertThat(paladin.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fellow attackers are tapped and do not count; only untapped creatures do")
    void tappedAttackersDoNotCount() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        addCreatureReady(player1, createCreatureCard("Fellow Attacker"));
        addCreatureReady(player1, createCreatureCard("Stay Home"));

        // Paladin (0) and Fellow Attacker (1) attack and tap; Stay Home (2) is untapped.
        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(paladin.getPowerModifier()).isEqualTo(1);
        assertThat(paladin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's untapped creatures do not count")
    void opponentCreaturesDoNotCount() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        addCreatureReady(player2, createCreatureCard("Enemy Creature"));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(paladin.getPowerModifier()).isEqualTo(0);
        assertThat(paladin.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Modifier wears off at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        addCreatureReady(player1, createCreatureCard("Untapped A"));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(paladin.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(paladin.getPowerModifier()).isEqualTo(0);
        assertThat(paladin.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Helper methods =====

    private Card createCreatureCard(String name) {
        Card card = new Card() {};
        card.setName(name);
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
