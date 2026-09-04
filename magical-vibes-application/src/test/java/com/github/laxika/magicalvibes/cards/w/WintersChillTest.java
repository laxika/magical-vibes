package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WintersChill.class, BalduvianBears.class, SnowCoveredIsland.class})
class WintersChillTest extends BaseCardTest {

    @Test
    @DisplayName("Pay nothing schedules destroy at end of combat")
    void payNothingSchedulesDestroy() {
        Permanent attacker = addAttacker(player2);
        addSnowLand(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        castAtDeclareAttackers(1, List.of(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player2, ChoiceContext.WintersChillPaymentChoice.PAY_NOTHING);

        boolean stillScheduled = gd.getDelayedActions(DelayedPermanentAction.class).stream()
                .anyMatch(a -> a.permanentId().equals(attacker.getId())
                        && a.kind() == DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT);
        boolean alreadyDestroyed = gd.playerGraveyards.get(player2.getId()).stream()
                .anyMatch(c -> c.getId().equals(attacker.getCard().getId()));
        assertThat(stillScheduled || alreadyDestroyed).isTrue();
    }

    @Test
    @DisplayName("Pay {1} prevents combat damage to and by the creature")
    void payOnePreventsCombatDamage() {
        Permanent attacker = addAttacker(player2);
        addSnowLand(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        castAtDeclareAttackers(1, List.of(attacker.getId()));

        harness.passBothPriorities();
        stopAtDeclareBlockers();
        harness.handleListChoice(player2, ChoiceContext.WintersChillPaymentChoice.PAY_ONE);

        assertThat(gd.creaturesWithCombatDamagePrevented).contains(attacker.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Pay {1} prevents combat damage dealt to and by the creature")
    void payOnePreventsCombatDamageInCombat() {
        Permanent attacker = addAttacker(player2);
        Permanent blocker = addCreatureReady(player1, new BalduvianBears());
        addSnowLand(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        castAtDeclareAttackers(1, List.of(attacker.getId()));

        harness.passBothPriorities();
        stopAtDeclareBlockers();
        harness.handleListChoice(player2, ChoiceContext.WintersChillPaymentChoice.PAY_ONE);

        prepareDeclareBlockers(player2);
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Pay {1} prevention ends when combat ends")
    void payOnePreventionEndsWithCombat() {
        Permanent attacker = addAttacker(player2);
        addSnowLand(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        castAtDeclareAttackers(1, List.of(attacker.getId()));

        harness.passBothPriorities();
        stopAtDeclareBlockers();
        harness.handleListChoice(player2, ChoiceContext.WintersChillPaymentChoice.PAY_ONE);
        assertThat(gd.creaturesWithCombatDamagePrevented).contains(attacker.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);

        assertThat(gd.creaturesWithCombatDamagePrevented).doesNotContain(attacker.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Pay {2} leaves the creature unaffected")
    void payTwoLeavesUnaffected() {
        Permanent attacker = addAttacker(player2);
        addSnowLand(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        castAtDeclareAttackers(1, List.of(attacker.getId()));

        harness.passBothPriorities();
        harness.handleListChoice(player2, ChoiceContext.WintersChillPaymentChoice.PAY_TWO);

        assertThat(gd.creaturesWithCombatDamagePrevented).doesNotContain(attacker.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("Each targeted creature gets its own payment choice")
    void resolvesEachTargetIndependently() {
        Permanent firstAttacker = addAttacker(player2);
        Permanent secondAttacker = addAttacker(player2);
        addSnowLand(player1);
        addSnowLand(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        castAtDeclareAttackers(2, List.of(firstAttacker.getId(), secondAttacker.getId()));

        harness.passBothPriorities();
        stopAtDeclareBlockers();
        harness.handleListChoice(player2, ChoiceContext.WintersChillPaymentChoice.PAY_ONE);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player2, ChoiceContext.WintersChillPaymentChoice.PAY_TWO);

        assertThat(gd.creaturesWithCombatDamagePrevented).contains(firstAttacker.getId())
                .doesNotContain(secondAttacker.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(firstAttacker.getId())
                .doesNotContain(secondAttacker.getId());
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("X=0 resolves without snow lands or targets")
    void zeroXResolvesWithoutSnowLandsOrTargets() {
        castAtDeclareAttackers(0, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot announce X greater than snow lands controlled")
    void cannotExceedSnowLandCap() {
        Permanent attacker = addAttacker(player2);
        addSnowLand(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new WintersChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X can't be greater");
    }

    @Test
    @DisplayName("Cannot cast outside combat before blockers")
    void cannotCastOutsideTiming() {
        Permanent attacker = addAttacker(player2);
        addSnowLand(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WintersChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        addSnowLand(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new WintersChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(bystander.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No mana auto-applies pay nothing and destroys at end of combat")
    void noManaAutoDestroys() {
        Permanent attacker = addAttacker(player2);
        addSnowLand(player1);
        castAtDeclareAttackers(1, List.of(attacker.getId()));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(attacker.getCard().getId()));
    }

    private void castAtDeclareAttackers(int x, List<UUID> targets) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new WintersChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        if (x > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, x);
        }
        harness.castInstantForX(player1, 0, x, targets);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new BalduvianBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId().equals(owner.getId()) ? player2.getId() : player1.getId());
        return attacker;
    }

    private void addSnowLand(Player owner) {
        harness.addToBattlefieldAndReturn(owner, new SnowCoveredIsland());
    }

    private void stopAtDeclareBlockers() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.DECLARE_BLOCKERS));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.DECLARE_BLOCKERS));
    }
}
