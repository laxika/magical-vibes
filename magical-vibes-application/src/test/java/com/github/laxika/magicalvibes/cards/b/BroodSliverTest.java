package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.q.QuickSliver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodSliver.class, QuickSliver.class, FugitiveWizard.class, Humility.class, Dominate.class})
class BroodSliverTest extends BaseCardTest {

    @Test
    void combatDamageOffersToCreateAColorlessSliverToken() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new QuickSliver());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Sliver");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SLIVER);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void decliningTheAbilityCreatesNoToken() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new QuickSliver());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void nonSliverCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void blockedSliverDoesNotTrigger() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new QuickSliver());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new QuickSliver());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void controllerOfTheSliverThatDealtDamageChoosesAndGetsTheToken() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player2, new QuickSliver());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Sliver").getCard().isToken()).isTrue();
        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void eachSliverThatDealsCombatDamageCreatesItsOwnMayAbility() {
        addCreatureReady(player1, new BroodSliver());
        Permanent firstAttacker = addCreatureReady(player1, new QuickSliver());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new QuickSliver());
        secondAttacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sliver")).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenBroodSliverHasLostAllAbilities() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player2, new QuickSliver());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.addToBattlefield(player2, new Humility());

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void doesNotTriggerWhenAnOpponentIsDamagedAndBroodSliverHasLostAllAbilities() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new QuickSliver());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new Humility());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void broodSliversOwnCombatDamageCreatesOneTokenRegardlessOfDamageAmount() {
        Permanent attacker = addCreatureReady(player1, new BroodSliver());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sliver")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 17);
    }

    @Test
    void twoBroodSliversEachTriggerForTheSameDamagingSliver() {
        addCreatureReady(player1, new BroodSliver());
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new QuickSliver());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sliver")).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void newControllerOfFriendlyDamagingSliverChoosesAndReceivesToken() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new QuickSliver());
        attacker.setAttacking(true);
        harness.setHand(player2, List.of(new Dominate()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.castInstant(player2, 0, 2, attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Sliver")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void newControllerOfOpposingDamagingSliverChoosesAndReceivesToken() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player2, new QuickSliver());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.setHand(player1, List.of(new Dominate()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.castInstant(player1, 0, 2, attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sliver")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Sliver");
    }
}
