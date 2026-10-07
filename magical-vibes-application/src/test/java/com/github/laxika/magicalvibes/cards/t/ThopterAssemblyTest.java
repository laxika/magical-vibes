package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThopterAssembly.class, DivineOffering.class})
class ThopterAssemblyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns self to hand and creates five 1/1 Thopter tokens when no other Thopters controlled")
    void triggersWhenNoOtherThopters() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Thopter Assembly should be back in hand
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        harness.assertNotOnBattlefield(player1, "Thopter Assembly");

        harness.assertInHand(player1, "Thopter Assembly");

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

    @Test
    @DisplayName("Does not trigger when controller has another Thopter on the battlefield")
    void doesNotTriggerWithOtherThopter() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        // Add another Thopter creature
        ThopterAssembly otherThopter = new ThopterAssembly();
        harness.addToBattlefield(player1, otherThopter);

        advanceToUpkeep(player1);

        // Stack should be empty — trigger did not fire
        assertThat(gd.stack).isEmpty();

        // Thopter Assembly should still be on the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        harness.assertOnBattlefield(player1, "Thopter Assembly");

        // No tokens should be created
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Triggers even when opponent controls Thopters")
    void triggersWhenOpponentHasThopters() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        // Add a Thopter to opponent's battlefield
        ThopterAssembly opponentThopter = new ThopterAssembly();
        harness.addToBattlefield(player2, opponentThopter);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Thopter Assembly should be returned to hand
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        harness.assertNotOnBattlefield(player1, "Thopter Assembly");

        // Should have 5 tokens
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().isToken())
                .toList()).hasSize(5);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        advanceToUpkeep(player2);

        // Stack should be empty — UPKEEP_TRIGGERED only fires on controller's upkeep
        assertThat(gd.stack).isEmpty();

        harness.assertOnBattlefield(player1, "Thopter Assembly");
    }

    @Test
    @DisplayName("Does nothing if another Thopter enters before trigger resolves")
    void doesNothingIfConditionFailsAtResolution() {
        ThopterAssembly assembly = new ThopterAssembly();
        harness.addToBattlefield(player1, assembly);

        advanceToUpkeep(player1);

        // Trigger is on the stack. Now add another Thopter before resolving.
        assertThat(gd.stack).hasSize(1);

        ThopterAssembly anotherThopter = new ThopterAssembly();
        harness.addToBattlefield(player1, anotherThopter);

        harness.passBothPriorities(); // resolve trigger — condition no longer met

        // Thopter Assembly should still be on the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        harness.assertOnBattlefield(player1, "Thopter Assembly");

        // No tokens should be created
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().isToken())
                .toList()).isEmpty();
    }

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
        harness.assertNotInHand(player1, "Thopter Assembly");

        // Tokens should still be created
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(5);
    }

    @Test
    @DisplayName("Two Thopter Assemblies prevent each other from triggering")
    void twoAssembliesPreventEachOther() {
        harness.addToBattlefield(player1, new ThopterAssembly());
        harness.addToBattlefield(player1, new ThopterAssembly());

        advanceToUpkeep(player1);

        // Neither should trigger — each sees the other as "another Thopter"
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroying Assembly in response still creates tokens without returning the graveyard card")
    void destroyedSourceStillCreatesTokensThroughEngine() {
        Permanent assembly = harness.addToBattlefieldAndReturn(player1, new ThopterAssembly());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, assembly.getId());
        harness.assertInGraveyard(player1, "Thopter Assembly");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Thopter Assembly");
        harness.assertInGraveyard(player1, "Thopter Assembly");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(5).allMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("A controlled Assembly returns to its owner while its controller creates the tokens")
    void returnsToOwnerAndCreatesTokensForController() {
        Permanent assembly = harness.addToBattlefieldAndReturn(player1, new ThopterAssembly());
        gd.stolenCreatures.put(assembly.getId(), player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Thopter Assembly");
        harness.assertNotInHand(player1, "Thopter Assembly");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(5).allMatch(p -> p.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A departed source does not create tokens if another Thopter is controlled at resolution")
    void departedSourceStillChecksForOtherThopters() {
        Permanent assembly = harness.addToBattlefieldAndReturn(player1, new ThopterAssembly());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player1, 0, assembly.getId());
        harness.addToBattlefield(player1, new ThopterAssembly());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thopter Assembly");
        harness.assertNotInHand(player1, "Thopter Assembly");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1).noneMatch(p -> p.getCard().isToken());
    }
}
