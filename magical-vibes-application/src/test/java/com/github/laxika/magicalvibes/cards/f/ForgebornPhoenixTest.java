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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForgebornPhoenix.class, GrizzlyBears.class, LightningBolt.class, WebspinnerCuff.class,
        TezzeretBetrayerOfFlesh.class})
class ForgebornPhoenixTest extends BaseCardTest {

    @Test
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
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent phoenix = addReadyPhoenix();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(phoenix.getAttachedTo()).isNull();
    }

    @Test
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
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(phoenix.getCard().getId())))
                .hasSize(2);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Forgeborn Phoenix").isTapped()).isTrue();
    }

    @Test
    void oldReturnTriggerCannotReturnPhoenixAfterItReturnsAndDiesAgain() {
        Permanent phoenix = addReadyPhoenix();
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(firstAttacker);
        attachEquipment(secondAttacker);
        destroyPhoenix(phoenix);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
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
}
