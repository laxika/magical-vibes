package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.TezzeretBetrayerOfFlesh;
import com.github.laxika.magicalvibes.cards.w.WebspinnerCuff;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForgebornPhoenix.class, GrizzlyBears.class, LightningBolt.class, WebspinnerCuff.class,
        TezzeretBetrayerOfFlesh.class})
class ForgebornPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gains flying and Phoenix stops being a creature")
    void equippedCreatureGainsFlyingAndPhoenixStopsBeingACreature() {
        Permanent phoenix = addReadyPhoenix();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(phoenix.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, phoenix)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(phoenix.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, phoenix)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Reconfigure cannot target an opponent's creature")
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent phoenix = addReadyPhoenix();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(phoenix.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Phoenix returns tapped after its own death when an equipped creature deals combat damage")
    void phoenixReturnsTappedAfterItsOwnDeathWhenAnEquippedCreatureDealsCombatDamage() {
        Permanent phoenix = addReadyPhoenix();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(attacker);
        destroyPhoenix(phoenix);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Forgeborn Phoenix");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Dying equipped creature gains the return ability instead of Phoenix")
    void dyingEquippedCreatureGainsTheReturnAbilityInsteadOfPhoenix() {
        Permanent phoenix = addReadyPhoenix();
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        phoenix.setAttachedTo(dyingCreature.getId());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(attacker);

        dyingCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(dyingCreature.getCard().getId());
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(dyingCreature.getCard());
        assertThat(findPermanent(player1, "Forgeborn Phoenix")).isSameAs(phoenix);
    }

    @Test
    @DisplayName("Phoenix returns after an equipped creature deals combat damage to a planeswalker")
    void phoenixReturnsAfterAnEquippedCreatureDealsCombatDamageToAPlaneswalker() {
        Permanent phoenix = addReadyPhoenix();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(attacker);
        destroyPhoenix(phoenix);

        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(attackerIndex), Map.of(attackerIndex, planeswalker.getId()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(planeswalker.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY))
                .isEqualTo(1);
        assertThat(findPermanent(player1, "Forgeborn Phoenix").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each death adds another independent return ability")
    void eachDeathAddsAnotherIndependentReturnAbility() {
        Permanent phoenix = addReadyPhoenix();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(attacker);
        destroyPhoenix(phoenix);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Forgeborn Phoenix");
        destroyPhoenix(returned);

        attacker.untap();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> declareAttackers(
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(phoenix.getCard().getId())))
                .hasSize(2);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Forgeborn Phoenix").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Old return trigger cannot return Phoenix after it returns and dies again")
    void oldReturnTriggerCannotReturnPhoenixAfterItReturnsAndDiesAgain() {
        Permanent phoenix = addReadyPhoenix();
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(firstAttacker);
        attachEquipment(secondAttacker);
        destroyPhoenix(phoenix);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker))));
        resolveCombat();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Forgeborn Phoenix");
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Forgeborn Phoenix");
        harness.assertNotOnBattlefield(player1, "Forgeborn Phoenix");
    }

    @Test
    @DisplayName("Unequipped creature combat damage does not return Phoenix")
    void unequippedCreatureCombatDamageDoesNotReturnPhoenix() {
        Permanent phoenix = addReadyPhoenix();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        destroyPhoenix(phoenix);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Forgeborn Phoenix");
        harness.assertNotOnBattlefield(player1, "Forgeborn Phoenix");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's equipped creature combat damage does not return Phoenix")
    void opponentsEquippedCreatureCombatDamageDoesNotReturnPhoenix() {
        Permanent phoenix = addReadyPhoenix();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WebspinnerCuff());
        equipment.setAttachedTo(attacker.getId());
        destroyPhoenix(phoenix);

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Forgeborn Phoenix");
        harness.assertNotOnBattlefield(player1, "Forgeborn Phoenix");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Reconfigure cannot be activated during combat")
    void reconfigureCannotBeActivatedDuringCombat() {
        Permanent phoenix = addReadyPhoenix();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(phoenix.getAttachedTo()).isNull();
    }

    private Permanent addReadyPhoenix() {
        return addCreatureReady(player1, new ForgebornPhoenix());
    }

    private void attachEquipment(Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new WebspinnerCuff());
        equipment.setAttachedTo(creature.getId());
    }

    private void destroyPhoenix(Permanent phoenix) {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, phoenix.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("A Phoenix exiled from the graveyard before its death ability resolves gains nothing (CR 400.7)")
    void phoenixExiledFromTheGraveyardGainsNothing() {
        Permanent phoenix = addReadyPhoenix();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, phoenix.getId());
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(phoenix.getCard()));
        resolveAllTriggers();

        assertThat(gd.perpetualAbilities(phoenix.getCard().getId())).isEmpty();
    }

    @Test
    @DisplayName("An equipped creature card exiled from the graveyard before the ability resolves gains nothing (CR 400.7)")
    void equippedCreatureCardExiledFromTheGraveyardGainsNothing() {
        Permanent phoenix = addReadyPhoenix();
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        phoenix.setAttachedTo(dyingCreature.getId());

        dyingCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(dyingCreature.getCard()));
        resolveAllTriggers();

        assertThat(gd.perpetualAbilities(dyingCreature.getCard().getId())).isEmpty();
        assertThat(gd.perpetualAbilities(phoenix.getCard().getId())).isEmpty();
    }
}
