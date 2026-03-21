package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MinimumAttackersConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarcryPhoenixTest extends BaseCardTest {

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }

    @Test
    @DisplayName("Has GRAVEYARD_ON_ALLY_CREATURES_ATTACK effect with MinimumAttackersConditionalEffect wrapping MayPayManaEffect")
    void hasCorrectEffects() {
        WarcryPhoenix card = new WarcryPhoenix();

        assertThat(card.getEffects(EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK).getFirst())
                .isInstanceOf(MinimumAttackersConditionalEffect.class);
        MinimumAttackersConditionalEffect mac = (MinimumAttackersConditionalEffect)
                card.getEffects(EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK).getFirst();
        assertThat(mac.minimumAttackers()).isEqualTo(3);
        assertThat(mac.wrapped()).isInstanceOf(MayPayManaEffect.class);
        MayPayManaEffect mayPay = (MayPayManaEffect) mac.wrapped();
        assertThat(mayPay.manaCost()).isEqualTo("{2}{R}");
        assertThat(mayPay.wrapped()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect returnEffect = (ReturnCardFromGraveyardEffect) mayPay.wrapped();
        assertThat(returnEffect.enterTapped()).isTrue();
        assertThat(returnEffect.enterAttacking()).isTrue();
    }

    @Test
    @DisplayName("Triggers when attacking with 3 creatures and Phoenix is in graveyard")
    void triggersWithThreeAttackers() {
        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);

        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear2);

        Permanent bear3 = new Permanent(new GrizzlyBears());
        bear3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear3);

        harness.setGraveyard(player1, List.of(new WarcryPhoenix()));

        declareAttackers(List.of(0, 1, 2));

        // Resolve the MayPayManaEffect from stack
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getName()).isEqualTo("Warcry Phoenix");
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{2}{R}");
    }

    @Test
    @DisplayName("Does not trigger when attacking with only 2 creatures")
    void doesNotTriggerWithTwoAttackers() {
        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);

        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear2);

        harness.setGraveyard(player1, List.of(new WarcryPhoenix()));

        declareAttackers(List.of(0, 1));

        // No trigger should have been pushed
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack.stream().noneMatch(
                e -> e.getCard().getName().equals("Warcry Phoenix"))).isTrue();
    }

    @Test
    @DisplayName("Accepting and paying {2}{R} returns Phoenix to battlefield tapped and attacking")
    void acceptingReturnsPhoenixTappedAndAttacking() {
        WarcryPhoenix phoenix = new WarcryPhoenix();

        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);

        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear2);

        Permanent bear3 = new Permanent(new GrizzlyBears());
        bear3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear3);

        harness.setGraveyard(player1, List.of(phoenix));
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackers(List.of(0, 1, 2));

        // Resolve the MayPayManaEffect from stack
        harness.passBothPriorities();
        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        // Phoenix should be on the battlefield — it entered tapped and attacking
        // (auto-pass advances past END_OF_COMBAT, clearing isAttacking; use attackedThisTurn instead)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(phoenix.getId()));
        Permanent phoenixPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(phoenix.getId()))
                .findFirst().orElseThrow();
        assertThat(phoenixPerm.isTapped()).isTrue();
        assertThat(phoenixPerm.isAttackedThisTurn()).isTrue();

        // Verify the game log recorded "tapped and attacking"
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Warcry Phoenix") && log.contains("tapped and attacking"));

        // Phoenix should no longer be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Declining keeps Phoenix in graveyard")
    void decliningKeepsPhoenixInGraveyard() {
        WarcryPhoenix phoenix = new WarcryPhoenix();

        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);

        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear2);

        Permanent bear3 = new Permanent(new GrizzlyBears());
        bear3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear3);

        harness.setGraveyard(player1, List.of(phoenix));
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackers(List.of(0, 1, 2));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Phoenix should still be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phoenix.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Warcry Phoenix"));
    }

    @Test
    @DisplayName("Does not trigger when Phoenix is on the battlefield (not in graveyard)")
    void doesNotTriggerFromBattlefield() {
        Permanent phoenix = new Permanent(new WarcryPhoenix());
        phoenix.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(phoenix);

        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);

        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear2);

        // Phoenix is on the battlefield, not in the graveyard
        declareAttackers(List.of(0, 1, 2));

        // No graveyard trigger should fire
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during opponent's attack even with 3+ attackers")
    void doesNotTriggerDuringOpponentAttack() {
        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bear1);

        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bear2);

        Permanent bear3 = new Permanent(new GrizzlyBears());
        bear3.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bear3);

        // Phoenix in player1's graveyard, but player2 is attacking
        harness.setGraveyard(player1, List.of(new WarcryPhoenix()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player2, List.of(0, 1, 2));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack.stream().noneMatch(
                e -> e.getCard().getName().equals("Warcry Phoenix"))).isTrue();
    }

    @Test
    @DisplayName("Cannot return if player cannot pay {2}{R}")
    void cannotReturnWithoutMana() {
        WarcryPhoenix phoenix = new WarcryPhoenix();

        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);

        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear2);

        Permanent bear3 = new Permanent(new GrizzlyBears());
        bear3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear3);

        harness.setGraveyard(player1, List.of(phoenix));
        // No mana added

        declareAttackers(List.of(0, 1, 2));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Phoenix stays in graveyard because mana cannot be paid
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phoenix.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Warcry Phoenix"));
    }
}
