package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.s.SimicRagworm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VigeanHydropon.class, SimicRagworm.class, AzoriusSignet.class})
class VigeanHydroponTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five +1/+1 counters")
    void entersWithFiveCounters() {
        Permanent hydropon = castHydropon(player1);

        assertThat(hydropon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Graft moves a counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent hydropon = castHydropon(player1);
        Permanent ragworm = castRagworm(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(hydropon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ragworm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may move a counter onto an opponent's creature that enters")
    void graftMovesCounterOntoOpponentsCreature() {
        Permanent hydropon = castHydropon(player1);
        Permanent ragworm = castRagworm(player2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(hydropon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ragworm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may be declined")
    void graftMayBeDeclined() {
        Permanent hydropon = castHydropon(player1);
        Permanent ragworm = castRagworm(player1);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(hydropon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(ragworm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft does not trigger for a noncreature entering")
    void graftDoesNotTriggerForNoncreatureEntering() {
        Permanent hydropon = castHydropon(player1);

        harness.castFromHand(player1, new AzoriusSignet(), "{2}");
        harness.passBothPriorities();
        Permanent signet = findPermanent(player1, "Azorius Signet");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(hydropon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(signet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot attack")
    void cannotAttack() {
        Permanent hydropon = castHydropon(player1);
        hydropon.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        Permanent attacker = addCreatureReady(player1, new SimicRagworm());
        attacker.setAttacking(true);
        castHydropon(player2);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Moving the last graft counter puts Hydropon into the graveyard")
    void movingLastCounterCausesHydroponToDie() {
        Permanent hydropon = castHydropon(player1);
        hydropon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent ragworm = castRagworm(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ragworm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Vigean Hydropon");
        harness.assertInGraveyard(player1, "Vigean Hydropon");
    }

    @Test
    @DisplayName("Graft cannot move a counter when Hydropon has left the battlefield")
    void graftDoesNothingWhenSourceHasLeft() {
        Permanent hydropon = castHydropon(player1);
        Permanent ragworm = castRagworm(player1);
        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).remove(hydropon);
        gd.playerGraveyards.get(player1.getId()).add(hydropon.getCard());

        harness.passBothPriorities();

        assertThat(ragworm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft does not remove a counter when the entering creature has left")
    void graftDoesNothingWhenEnteringCreatureHasLeft() {
        Permanent hydropon = castHydropon(player1);
        Permanent ragworm = castRagworm(player1);
        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).remove(ragworm);
        gd.playerGraveyards.get(player1.getId()).add(ragworm.getCard());

        harness.passBothPriorities();

        assertThat(hydropon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    private Permanent castHydropon(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new VigeanHydropon(), "{1}{G}{U}");
        harness.passBothPriorities();
        return findPermanent(player, "Vigean Hydropon");
    }

    private Permanent castRagworm(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new SimicRagworm(), "{3}{G}");
        harness.passBothPriorities();
        return findPermanent(player, "Simic Ragworm");
    }
}
