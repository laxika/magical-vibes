package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
        harness.handleListChoice(player2, ChoiceContext.WintersChillPaymentChoice.PAY_ONE);

        assertThat(gd.creaturesWithCombatDamagePrevented).contains(attacker.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
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
    @DisplayName("Cannot announce X greater than snow lands controlled")
    void cannotExceedSnowLandCap() {
        Permanent attacker = addAttacker(player2);
        addSnowLand(player1);
        harness.forceActivePlayer(player1);
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WintersChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSnowLand(player1);
        harness.forceActivePlayer(player1);
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
        // Auto-pass may cascade through end of combat and drain the delayed destroy.
        boolean stillScheduled = gd.getDelayedActions(DelayedPermanentAction.class).stream()
                .anyMatch(a -> a.permanentId().equals(attacker.getId())
                        && a.kind() == DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT);
        boolean alreadyDestroyed = gd.playerGraveyards.get(player2.getId()).stream()
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(stillScheduled || alreadyDestroyed).isTrue();
    }

    private void castAtDeclareAttackers(int x, List<UUID> targets) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new WintersChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        if (x > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, x);
        }
        harness.castInstantForX(player1, 0, x, targets);
    }

    private Permanent addAttacker(Player owner) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(player1.getId().equals(owner.getId()) ? player2.getId() : player1.getId());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private void addSnowLand(Player owner) {
        Permanent land = harness.addToBattlefieldAndReturn(owner, new Island());
        TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
    }
}
