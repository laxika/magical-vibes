package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AntiqueCollector.class, GrizzlyBears.class, HillGiant.class})
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
