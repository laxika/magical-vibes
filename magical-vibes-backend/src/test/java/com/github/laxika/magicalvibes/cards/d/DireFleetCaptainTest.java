package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOtherAttackingSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DireFleetCaptainTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK trigger with BoostSelfPerOtherAttackingSubtypeEffect(PIRATE, 1, 1)")
    void hasCorrectStructure() {
        DireFleetCaptain card = new DireFleetCaptain();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(BoostSelfPerOtherAttackingSubtypeEffect.class);
        BoostSelfPerOtherAttackingSubtypeEffect effect = (BoostSelfPerOtherAttackingSubtypeEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.PIRATE);
        assertThat(effect.powerPerCreature()).isEqualTo(1);
        assertThat(effect.toughnessPerCreature()).isEqualTo(1);
    }

    // ===== Attack trigger fires =====

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new DireFleetCaptain());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Dire Fleet Captain"));
    }

    // ===== Boost based on other attacking Pirates =====

    @Test
    @DisplayName("Gets +0/+0 when attacking alone (no other Pirates)")
    void noBoostWhenAttackingAlone() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(0);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+1 when attacking with one other Pirate")
    void boostWithOneOtherPirate() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, createPirateCard("Test Pirate"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 when attacking with two other Pirates")
    void boostWithTwoOtherPirates() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, createPirateCard("Pirate A"));
        addCreatureReady(player1, createPirateCard("Pirate B"));

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(2);
        assertThat(captain.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Pirate attackers do not count")
    void nonPirateAttackersDoNotCount() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, createNonPirateCard("Goblin Grunt"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(0);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only counts attacking Pirates, not non-attacking ones")
    void onlyCountsAttackingPirates() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, createPirateCard("Attacking Pirate"));
        addCreatureReady(player1, createPirateCard("Staying Home Pirate"));

        // Only captain (index 0) and the first pirate (index 1) attack; second pirate (index 2) stays back
        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent captain = addCreatureReady(player1, new DireFleetCaptain());
        addCreatureReady(player1, createPirateCard("Test Pirate"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(captain.getPowerModifier()).isEqualTo(0);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createPirateCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.PIRATE));
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createNonPirateCard(String name) {
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
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
