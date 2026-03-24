package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SanctumSeekerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has Vampire-conditional EachOpponentLosesLifeEffect and GainLifeEffect on ON_ALLY_CREATURE_ATTACKS")
    void hasCorrectEffects() {
        SanctumSeeker card = new SanctumSeeker();

        List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects =
                card.getEffects(EffectSlot.ON_ALLY_CREATURE_ATTACKS);
        assertThat(effects).hasSize(2);

        // First effect: opponent life loss
        assertThat(effects.get(0)).isInstanceOf(SubtypeConditionalEffect.class);
        SubtypeConditionalEffect lossConditional = (SubtypeConditionalEffect) effects.get(0);
        assertThat(lossConditional.subtype()).isEqualTo(CardSubtype.VAMPIRE);
        assertThat(lossConditional.wrapped()).isInstanceOf(EachOpponentLosesLifeEffect.class);
        assertThat(((EachOpponentLosesLifeEffect) lossConditional.wrapped()).amount()).isEqualTo(1);

        // Second effect: controller life gain
        assertThat(effects.get(1)).isInstanceOf(SubtypeConditionalEffect.class);
        SubtypeConditionalEffect gainConditional = (SubtypeConditionalEffect) effects.get(1);
        assertThat(gainConditional.subtype()).isEqualTo(CardSubtype.VAMPIRE);
        assertThat(gainConditional.wrapped()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) gainConditional.wrapped()).amount()).isEqualTo(1);
    }

    // ===== Trigger: Vampire attacks =====

    @Test
    @DisplayName("Puts triggered ability on stack when a Vampire attacks")
    void triggersWhenVampireAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addSanctumSeekerReady(player1);
        addVampireCreatureReady(player1);

        declareAttackers(List.of(1)); // Vampire creature attacks (2/2)

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sanctum Seeker");

        harness.passBothPriorities(); // resolve trigger + combat auto-advances

        // Opponent: 20 - 1 (trigger) - 2 (combat damage from 2/2) = 17
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Controller: 20 + 1 (trigger) = 21
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Triggers for each attacking Vampire separately")
    void triggersPerVampire() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addSanctumSeekerReady(player1);
        addVampireCreatureReady(player1);
        addVampireCreatureReady(player1);

        declareAttackers(List.of(1, 2)); // both Vampires attack (each 2/2)

        // Two separate triggers on the stack (one per attacking Vampire)
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger + combat auto-advances

        // Opponent: 20 - 2 (2 triggers) - 4 (combat damage from two 2/2s) = 14
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        // Controller: 20 + 2 (2 triggers) = 22
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Sanctum Seeker itself is a Vampire — triggers when it attacks")
    void triggersWhenSanctumSeekerAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addSanctumSeekerReady(player1);

        declareAttackers(List.of(0)); // Sanctum Seeker attacks (3/4)

        // Trigger should be on stack
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger + combat

        // Opponent: 20 - 1 (trigger) - 3 (combat damage from 3/4) = 16
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Controller: 20 + 1 (trigger) = 21
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    // ===== No trigger: non-Vampire attackers =====

    @Test
    @DisplayName("Does not trigger when a non-Vampire creature attacks")
    void doesNotTriggerForNonVampire() {
        addSanctumSeekerReady(player1);
        addNonVampireCreatureReady(player1);

        declareAttackers(List.of(1)); // non-Vampire attacks

        // No trigger on the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers only for attacking Vampires in a mixed group")
    void triggersOnlyForVampiresInMixedGroup() {
        addSanctumSeekerReady(player1);
        addVampireCreatureReady(player1);
        addNonVampireCreatureReady(player1);

        declareAttackers(List.of(1, 2)); // Vampire + non-Vampire attack

        // Only 1 trigger (from the Vampire), not 2
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sanctum Seeker");
    }

    @Test
    @DisplayName("Does not trigger for opponent's attacking Vampires")
    void doesNotTriggerForOpponentVampires() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addSanctumSeekerReady(player1);
        addVampireCreatureReady(player2);

        // Opponent declares attackers
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player2, List.of(0));

        // Sanctum Seeker belongs to player1 — opponent's Vampires shouldn't trigger it
        // Stack should have no Sanctum Seeker triggers
        assertThat(gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Sanctum Seeker"))
                .count()).isZero();
    }

    // ===== Helpers =====

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }

    private Permanent addSanctumSeekerReady(Player player) {
        SanctumSeeker card = new SanctumSeeker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addVampireCreatureReady(Player player) {
        Card creature = new Card();
        creature.setName("Test Vampire");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{B}");
        creature.setColor(CardColor.BLACK);
        creature.setSubtypes(List.of(CardSubtype.VAMPIRE));
        creature.setPower(2);
        creature.setToughness(2);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addNonVampireCreatureReady(Player player) {
        Card creature = new GrizzlyBears();
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
