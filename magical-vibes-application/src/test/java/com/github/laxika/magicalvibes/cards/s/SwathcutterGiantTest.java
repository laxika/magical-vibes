package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AssassinsTrophy;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.g.GoblinBanneret;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.r.RalIzzetViceroy;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SwathcutterGiant.class, GrizzlyBears.class, HillGiant.class, GoblinBanneret.class,
        HitchclawRecluse.class, RalIzzetViceroy.class, AssassinsTrophy.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class SwathcutterGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Swathcutter Giant deals 1 damage to each defending creature")
    void attackTriggerDamagesDefendingCreatures() {
        addCreatureReady(player1, new SwathcutterGiant());
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent defendingHillGiant = addCreatureReady(player2, new HillGiant());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingBears.getMarkedDamage()).isEqualTo(1);
        assertThat(defendingHillGiant.getMarkedDamage()).isEqualTo(1);
        assertThat(ownBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attack damage kills one-toughness creatures before blockers are declared")
    void attackDamageKillsSmallCreaturesBeforeBlocking() {
        Permanent giant = addCreatureReady(player1, new SwathcutterGiant());
        addCreatureReady(player2, new GoblinBanneret());
        Permanent survivor = addCreatureReady(player2, new HitchclawRecluse());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Goblin Banneret");
        harness.assertInGraveyard(player2, "Goblin Banneret");
        assertThat(survivor.getMarkedDamage()).isEqualTo(1);
        assertThat(giant.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An attack trigger still deals damage after its source is destroyed")
    void attackTriggerSurvivesSourceRemoval() {
        Permanent giant = addCreatureReady(player1, new SwathcutterGiant());
        Permanent defender = addCreatureReady(player2, new HitchclawRecluse());
        harness.setHand(player2, List.of(new AssassinsTrophy()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Swathcutter Giant");
        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking a planeswalker damages its controller's creatures")
    void attackingPlaneswalkerDamagesItsControllersCreatures() {
        addCreatureReady(player1, new SwathcutterGiant());
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalIzzetViceroy());
        ral.setCounterCount(CounterType.LOYALTY, 5);
        Permanent defender = addCreatureReady(player2, new HitchclawRecluse());

        declareAttackAt(ral);
        resolveAllTriggers();

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing the attacked planeswalker does not stop damage to the defending creatures")
    void attackTriggerRemembersDefenderAfterPlaneswalkerLeaves() {
        addCreatureReady(player1, new SwathcutterGiant());
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalIzzetViceroy());
        ral.setCounterCount(CounterType.LOYALTY, 5);
        Permanent defender = addCreatureReady(player2, new HitchclawRecluse());
        harness.setHand(player1, List.of(new AssassinsTrophy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttackAt(ral);
        harness.castAndResolveInstant(player1, 0, ral.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Ral, Izzet Viceroy");
        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking a battle damages its protector's creatures, not its controller's")
    void attackingBattleDamagesItsProtectorsCreatures() {
        Permanent giant = addCreatureReady(player1, new SwathcutterGiant());
        Permanent ownCreature = addCreatureReady(player1, new HitchclawRecluse());
        Permanent defender = addCreatureReady(player2, new HitchclawRecluse());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());

        declareAttackAt(battle);
        resolveAllTriggers();

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(giant.getMarkedDamage()).isZero();
    }

    private void declareAttackAt(Permanent attackedPermanent) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, attackedPermanent.getId()));
    }
}
