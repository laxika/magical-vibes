package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AntiqueCollector.class, GrizzlyBears.class, HillGiant.class, TreetopVillage.class})
class AntiqueCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by creatures with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new AntiqueCollector());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by creatures with power greater than 2")
    void canBeBlockedByHighPowerCreature() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent attacker = addCreatureReady(player1, new AntiqueCollector());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void grantsCurrentCreaturesADeathMayAbilityThatShufflesAndInvestigates() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();

        kill(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId()
                .equals(bears.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getId()
                .equals(bears.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.CLUE));
    }

    @Test
    void perpetualGrantSurvivesAntiqueCollectorsDeath() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent collector = harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();

        kill(collector);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        kill(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getId()
                .equals(bears.getCard().getId()));
    }

    @Test
    void decliningTheMayAbilityLeavesTheCreatureInTheGraveyard() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();

        kill(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId()
                .equals(bears.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card.getId()
                .equals(bears.getCard().getId()));
    }

    @Test
    void repeatedEntriesGrantSeparateDeathTriggers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();

        kill(bears);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void cannotShuffleACreatureInItsOwnersOpposingGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();

        kill(bears);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Clue")).isZero();
    }

    @Test
    void animatedLandReceivesTheDeathAbility() {
        Permanent village = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();

        village.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotInGraveyard(player1, "Treetop Village");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(village.getCard().getId()));
        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
    }

    @Test
    void creaturesEnteringAfterTheGrantDoNotReceiveTheAbility() {
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        kill(bears);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void opponentsCreaturesDoNotReceiveTheAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();

        kill(bears);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void creatureLeavingTheGraveyardBeforeResolutionDoesNotInvestigate() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();
        kill(bears);
        gd.playerGraveyards.get(player1.getId()).remove(bears.getCard());
        harness.setExile(player1, java.util.List.of(bears.getCard()));

        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Clue")).isZero();
    }

    @Test
    void deathAbilityPersistsAfterShufflingAndReentering() {
        harness.setLibrary(player1, new ArrayList<>());
        GrizzlyBears card = new GrizzlyBears();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, card);
        harness.enterBattlefieldAndReturn(player1, new AntiqueCollector());
        resolveAllTriggers();
        kill(bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gd.playerDecks.get(player1.getId()).remove(card);
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);
        resolveAllTriggers();

        kill(returned);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(libraryCard -> libraryCard.getId().equals(card.getId()));
        assertThat(countPermanents(player1, "Clue")).isEqualTo(2);
    }

    private void kill(Permanent permanent) {
        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2,
                java.util.List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
