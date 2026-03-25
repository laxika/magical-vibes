package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackControllerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TroveOfTemptationTest extends BaseCardTest {

    // ===== Effect structure =====

    @Test
    @DisplayName("Has OpponentsMustAttackControllerEffect on STATIC slot")
    void hasCorrectStaticEffect() {
        TroveOfTemptation card = new TroveOfTemptation();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(OpponentsMustAttackControllerEffect.class);
    }

    @Test
    @DisplayName("Has CreateTokenEffect (Treasure) on CONTROLLER_END_STEP_TRIGGERED slot")
    void hasCorrectEndStepEffect() {
        TroveOfTemptation card = new TroveOfTemptation();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Treasure");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts enchantment on stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TroveOfTemptation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Trove of Temptation");
    }

    // ===== Must attack requirement =====

    @Test
    @DisplayName("Opponent must attack with at least one creature when Trove is on the battlefield")
    void opponentMustAttackWithAtLeastOneCreature() {
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Declaring no attackers should fail
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must attack with at least one creature");
    }

    @Test
    @DisplayName("Opponent can successfully declare one creature as attacker")
    void opponentCanDeclareOneAttacker() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player2, List.of(0));

        // Bears (2/2) attacks — damage resolves later, just verify no exception
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Only requires one creature to attack even when multiple are available")
    void onlyOneCreatureRequiredToAttack() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Declaring just one attacker should succeed (unlike Curse of Nightly Hunt which requires ALL)
        gs.declareAttackers(gd, player2, List.of(1));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller's creatures are not forced to attack")
    void controllerNotForced() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Controller's creatures should NOT be forced
        gs.declareAttackers(gd, player1, List.of());
    }

    @Test
    @DisplayName("No creature forced to attack if all are tapped")
    void noForceIfAllTapped() {
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // All creatures are tapped — no attackable creatures, so step is skipped
        // (handleDeclareAttackersStep returns without setting ATTACKER_DECLARATION)
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.ATTACKER_DECLARATION);
    }

    @Test
    @DisplayName("Effect removed when Trove leaves the battlefield")
    void effectRemovedWhenTroveLeaves() {
        TroveOfTemptation trove = new TroveOfTemptation();
        harness.addToBattlefield(player1, trove);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        // Remove Trove from battlefield
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() == trove);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Now opponent can choose not to attack
        gs.declareAttackers(gd, player2, List.of());
    }

    // ===== End step Treasure token =====

    @Test
    @DisplayName("Creates Treasure token at controller's end step")
    void createsTreasureTokenAtEndStep() {
        harness.addToBattlefield(player1, new TroveOfTemptation());

        advanceToEndStep(player1);

        // Treasure token trigger should be on the stack
        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getCard().getName()).isEqualTo("Trove of Temptation"));

        harness.passBothPriorities();

        // Treasure token should be on the battlefield
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anySatisfy(p -> {
            assertThat(p.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(p.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        });
    }

    @Test
    @DisplayName("Does not create Treasure on opponent's end step")
    void noTreasureOnOpponentEndStep() {
        harness.addToBattlefield(player1, new TroveOfTemptation());

        int bfSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

        advanceToEndStep(player2);

        // No trigger should fire — stack should not contain Trove trigger
        assertThat(gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Trove of Temptation"))
                .count()).isZero();

        // Battlefield should not have gained any permanents
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(bfSizeBefore);
    }

    // ===== Helper methods =====

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN → END_STEP, triggers fire
    }
}
