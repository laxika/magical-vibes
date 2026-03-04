package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.NoOtherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAndCreateTokensEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThopterAssemblyTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Thopter Assembly has upkeep triggered ability with conditional wrapper")
    void hasUpkeepTriggeredAbility() {
        ThopterAssembly card = new ThopterAssembly();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoOtherSubtypeConditionalEffect.class);

        NoOtherSubtypeConditionalEffect conditional =
                (NoOtherSubtypeConditionalEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.THOPTER);
        assertThat(conditional.wrapped()).isInstanceOf(ReturnSelfToHandAndCreateTokensEffect.class);
    }

    // ===== Trigger fires when no other Thopters =====

    @Test
    @DisplayName("Returns self to hand and creates five 1/1 Thopter tokens when no other Thopters controlled")
    void triggersWhenNoOtherThopters() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Thopter Assembly should be back in hand
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Thopter Assembly"))
                .toList()).isEmpty();

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).anyMatch(c -> c.getName().equals("Thopter Assembly"));

        // Should have 5 Thopter tokens
        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(5);

        for (Permanent token : tokens) {
            assertThat(token.getCard().getName()).isEqualTo("Thopter");
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        }
    }

    // ===== Trigger does NOT fire when other Thopters present =====

    @Test
    @DisplayName("Does not trigger when controller has another Thopter on the battlefield")
    void doesNotTriggerWithOtherThopter() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        // Add another Thopter creature
        Card otherThopter = new Card();
        otherThopter.setName("Other Thopter");
        otherThopter.setType(CardType.CREATURE);
        otherThopter.setSubtypes(List.of(CardSubtype.THOPTER));
        otherThopter.setPower(1);
        otherThopter.setToughness(1);
        harness.addToBattlefield(player1, otherThopter);

        advanceToUpkeep(player1);

        // Stack should be empty — trigger did not fire
        assertThat(gd.stack).isEmpty();

        // Thopter Assembly should still be on the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Thopter Assembly"))
                .toList()).hasSize(1);

        // No tokens should be created
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().isToken())
                .toList()).isEmpty();
    }

    // ===== Opponent's Thopters don't prevent trigger =====

    @Test
    @DisplayName("Triggers even when opponent controls Thopters")
    void triggersWhenOpponentHasThopters() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        // Add a Thopter to opponent's battlefield
        Card opponentThopter = new Card();
        opponentThopter.setName("Opponent Thopter");
        opponentThopter.setType(CardType.CREATURE);
        opponentThopter.setSubtypes(List.of(CardSubtype.THOPTER));
        opponentThopter.setPower(1);
        opponentThopter.setToughness(1);
        harness.addToBattlefield(player2, opponentThopter);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Thopter Assembly should be returned to hand
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Thopter Assembly"))
                .toList()).isEmpty();

        // Should have 5 tokens
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().isToken())
                .toList()).hasSize(5);
    }

    // ===== Does not trigger during opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        advanceToUpkeep(player2);

        // Stack should be empty — UPKEEP_TRIGGERED only fires on controller's upkeep
        assertThat(gd.stack).isEmpty();

        // Thopter Assembly should still be on the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Thopter Assembly"))
                .toList()).hasSize(1);
    }

    // ===== Condition re-checked at resolution (intervening-if) =====

    @Test
    @DisplayName("Does nothing if another Thopter enters before trigger resolves")
    void doesNothingIfConditionFailsAtResolution() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        advanceToUpkeep(player1);

        // Trigger is on the stack. Now add another Thopter before resolving.
        assertThat(gd.stack).hasSize(1);

        Card anotherThopter = new Card();
        anotherThopter.setName("Sneaky Thopter");
        anotherThopter.setType(CardType.CREATURE);
        anotherThopter.setSubtypes(List.of(CardSubtype.THOPTER));
        anotherThopter.setPower(1);
        anotherThopter.setToughness(1);
        harness.addToBattlefield(player1, anotherThopter);

        harness.passBothPriorities(); // resolve trigger — condition no longer met

        // Thopter Assembly should still be on the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Thopter Assembly"))
                .toList()).hasSize(1);

        // No tokens should be created
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().isToken())
                .toList()).isEmpty();
    }

    // ===== Tokens still created if source leaves battlefield before resolution =====

    @Test
    @DisplayName("Still creates tokens if Thopter Assembly is destroyed before trigger resolves")
    void stillCreatesTokensIfDestroyedBeforeResolution() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        advanceToUpkeep(player1);

        // Trigger is on the stack
        assertThat(gd.stack).hasSize(1);

        // Simulate destroying Thopter Assembly before trigger resolves
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent assemblyPerm = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Thopter Assembly"))
                .findFirst().orElseThrow();
        battlefield.remove(assemblyPerm);

        harness.passBothPriorities(); // resolve trigger

        // Thopter Assembly is gone (destroyed), not in hand
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Thopter Assembly"));

        // Tokens should still be created
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(5);
    }

    // ===== Two Thopter Assemblies prevent each other from triggering =====

    @Test
    @DisplayName("Two Thopter Assemblies prevent each other from triggering")
    void twoAssembliesPreventEachOther() {
        harness.addToBattlefield(player1, new ThopterAssembly());
        harness.addToBattlefield(player1, new ThopterAssembly());

        advanceToUpkeep(player1);

        // Neither should trigger — each sees the other as "another Thopter"
        assertThat(gd.stack).isEmpty();
    }
}
