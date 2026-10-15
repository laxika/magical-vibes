package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ClericOfTheForwardOrder;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrowingKnife.class, ClericOfTheForwardOrder.class, Disperse.class})
class ThrowingKnifeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new ClericOfTheForwardOrder());
        attachKnife(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with the equipped creature and accepting sacrifices the knife for 2 damage to a player")
    void attackTriggerDamagesPlayer() {
        Permanent bears = addCreatureReady(player1, new ClericOfTheForwardOrder());
        attachKnife(player1, bears);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Throwing Knife");
        harness.assertInGraveyard(player1, "Throwing Knife");
        // 2 from the knife, then 2 unblocked combat damage from the now-unequipped Cleric.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The sacrificed knife can instead deal its 2 damage to a creature")
    void attackTriggerDamagesCreature() {
        Permanent bears = addCreatureReady(player1, new ClericOfTheForwardOrder());
        attachKnife(player1, bears);
        Permanent blocker = addCreatureReady(player2, new ClericOfTheForwardOrder());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Throwing Knife");
        harness.assertInGraveyard(player2, "Cleric of the Forward Order");
    }

    @Test
    @DisplayName("Declining the may leaves the knife attached and deals no damage")
    void decliningKeepsKnife() {
        Permanent bears = addCreatureReady(player1, new ClericOfTheForwardOrder());
        Permanent knife = attachKnife(player1, bears);

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Throwing Knife");
        assertThat(knife.getAttachedTo()).isEqualTo(bears.getId());
        // Only the equipped Cleric' 4 unblocked combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The knife does not trigger when its equipped creature stays home")
    void noTriggerWithoutAttack() {
        Permanent bears = addCreatureReady(player1, new ClericOfTheForwardOrder());
        addCreatureReady(player1, new ClericOfTheForwardOrder());
        attachKnife(player1, bears);

        declareAttackers(player1, List.of(1));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Throwing Knife");
        // Only the unequipped attacker's 2 combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void equipPaysTwoManaAndAttachesOnResolution() {
        Permanent knife = harness.addToBattlefieldAndReturn(player1, new ThrowingKnife());
        Permanent creature = addCreatureReady(player1, new ClericOfTheForwardOrder());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(knife.getAttachedTo()).isNull();
        harness.passBothPriorities();

        assertThat(knife.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new ThrowingKnife());
        Permanent creature = addCreatureReady(player2, new ClericOfTheForwardOrder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new ThrowingKnife());
        Permanent creature = addCreatureReady(player1, new ClericOfTheForwardOrder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingKnifeBeforeResolutionPreventsDamage() {
        Permanent creature = addCreatureReady(player1, new ClericOfTheForwardOrder());
        Permanent knife = attachKnife(player1, creature);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castAndResolveInstant(player1, 0, knife.getId());
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }
        resolveCombat();

        harness.assertInHand(player1, "Throwing Knife");
        harness.assertNotInGraveyard(player1, "Throwing Knife");
        harness.assertLife(player2, 18);
    }

    @Test
    void removingTargetBeforeResolutionKeepsKnife() {
        Permanent creature = addCreatureReady(player1, new ClericOfTheForwardOrder());
        Permanent knife = attachKnife(player1, creature);
        Permanent target = addCreatureReady(player2, new ClericOfTheForwardOrder());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(knife.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertOnBattlefield(player1, "Throwing Knife");
        harness.assertNotInGraveyard(player1, "Throwing Knife");
        harness.assertInHand(player2, "Cleric of the Forward Order");
        harness.assertLife(player2, 16);
    }

    @Test
    void knifeControllerControlsTriggerWhenOpponentsCreatureAttacks() {
        Permanent creature = addCreatureReady(player2, new ClericOfTheForwardOrder());
        attachKnife(player1, creature);

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Throwing Knife");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 18);
    }

    private Permanent attachKnife(Player player, Permanent host) {
        Permanent knife = harness.addToBattlefieldAndReturn(player, new ThrowingKnife());
        knife.setAttachedTo(host.getId());
        return knife;
    }
}
