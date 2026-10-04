package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InkfathomInfiltrator;
import com.github.laxika.magicalvibes.cards.p.PowerOfFire;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HelmOfTheGhastlord.class, FugitiveWizard.class, Forest.class, GrizzlyBears.class,
        ScatheZombies.class, InkfathomInfiltrator.class, PowerOfFire.class})
class HelmOfTheGhastlordTest extends BaseCardTest {

    @Test
    @DisplayName("Helm resolves attached to a targeted opponent's creature")
    void resolvesAttachedToOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new HelmOfTheGhastlord()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Helm of the Ghastlord").getAttachedTo())
                .isEqualTo(creature.getId());
    }


    @Test
    @DisplayName("Blue enchanted creature dealing combat damage draws a card for its controller")
    void blueCreatureDrawsOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new FugitiveWizard());
        attachHelm(player1, creature);
        creature.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        // Creature is not black, so the opponent does not discard.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Blue enchanted creature gets +1/+1")
    void blueCreatureGetsBoost() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new FugitiveWizard()); // 1/1
        attachHelm(player1, creature);
        creature.setAttacking(true);

        resolveCombatAndTrigger();

        // 1/1 boosted to 2/2 deals 2 combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }


    @Test
    @DisplayName("Black enchanted creature dealing combat damage makes the damaged player discard")
    void blackCreatureCausesDiscardOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new ScatheZombies());
        attachHelm(player1, creature);
        creature.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        // Controller does not draw (creature is not blue).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
    }


    @Test
    @DisplayName("Non-blue, non-black enchanted creature gets no boost and no triggered ability")
    void otherColorCreatureUnaffected() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears()); // 2/2 green
        attachHelm(player1, creature);
        creature.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();

        // No +1/+1: 2/2 deals exactly 2 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // No draw, no discard.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("A blue-black creature receives both boosts and triggers both abilities")
    void blueBlackCreatureGetsBothBonuses() {
        Permanent creature = addCreatureReady(player1, new InkfathomInfiltrator());
        attachHelm(player1, creature);
        creature.setAttacking(true);
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();

        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The two granted abilities use separate stack entries")
    void blueBlackCreatureHasTwoSeparateTriggers() {
        Permanent creature = addCreatureReady(player1, new InkfathomInfiltrator());
        attachHelm(player1, creature);
        creature.setAttacking(true);
        harness.setHand(player2, List.of(new Forest(), new Forest()));

        resolveCombat();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's enchanted blue creature draws for its own controller")
    void opposingCreatureControllerDraws() {
        Permanent creature = addCreatureReady(player2, new FugitiveWizard());
        attachHelm(player1, creature);
        creature.setAttacking(true);
        int auraControllerHand = gd.playerHands.get(player1.getId()).size();
        int creatureControllerHand = gd.playerHands.get(player2.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHand);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(creatureControllerHand + 1);
    }

    @Test
    @DisplayName("Blue creatures draw for noncombat damage to an opponent")
    void blueCreatureDrawsOnNoncombatDamage() {
        Permanent creature = addCreatureReady(player1, new FugitiveWizard());
        attachHelm(player1, creature);
        harness.addToBattlefieldAndReturn(player1, new PowerOfFire()).setAttachedTo(creature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Black creatures make the opponent discard for noncombat damage")
    void blackCreatureDiscardsOnNoncombatDamage() {
        Permanent creature = addCreatureReady(player1, new ScatheZombies());
        attachHelm(player1, creature);
        harness.addToBattlefieldAndReturn(player1, new PowerOfFire()).setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Damage to the enchanted creature's controller grants neither trigger")
    void damageToOwnControllerDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new InkfathomInfiltrator());
        attachHelm(player1, creature);
        harness.addToBattlefieldAndReturn(player1, new PowerOfFire()).setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void attachHelm(Player controller, Permanent creature) {
        Permanent helm = harness.addToBattlefieldAndReturn(controller, new HelmOfTheGhastlord());
        helm.setAttachedTo(creature.getId());
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
