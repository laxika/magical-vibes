package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DesertersDisciple;
import com.github.laxika.magicalvibes.cards.k.KyoshiBattleFan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BuzzardWaspColony.class, DesertersDisciple.class, KyoshiBattleFan.class})
class BuzzardWaspColonyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice an artifact or creature and draw a card")
    void etbSacrificesCreatureAndDraws() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DesertersDisciple());
        harness.setLibrary(player1, List.of(new DesertersDisciple()));
        harness.castFromHand(player1, new BuzzardWaspColony(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Deserter's Disciple");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the ETB sacrifice does not draw")
    void decliningEtbSacrificeDoesNotDraw() {
        harness.setLibrary(player1, List.of(new DesertersDisciple()));
        harness.castFromHand(player1, new BuzzardWaspColony(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A countered allied creature's death moves all counters to the colony")
    void counteredAllyDeathMovesAllCountersToColony() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new BuzzardWaspColony());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new DesertersDisciple());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dying.setCounterCount(CounterType.CHARGE, 2);
        dying.setMarkedDamage(3);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Deserter's Disciple");
        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(colony.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The sacrifice and draw happen during the same entry ability resolution")
    void drawsImmediatelyAfterSacrifice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DesertersDisciple());
        DesertersDisciple drawn = new DesertersDisciple();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new BuzzardWaspColony(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entry ability can sacrifice a noncreature artifact")
    void etbSacrificesArtifactAndDraws() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new KyoshiBattleFan());
        DesertersDisciple drawn = new DesertersDisciple();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new BuzzardWaspColony(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kyoshi Battle Fan");
        harness.assertOnBattlefield(player1, "Buzzard-Wasp Colony");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The colony can sacrifice itself to its entry ability and still draw")
    void etbCanSacrificeItself() {
        DesertersDisciple drawn = new DesertersDisciple();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new BuzzardWaspColony(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Buzzard-Wasp Colony"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Buzzard-Wasp Colony");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A creature without counters does not trigger the colony")
    void allyWithoutCountersDoesNotTrigger() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new BuzzardWaspColony());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new DesertersDisciple());
        dying.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deserter's Disciple");
        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's creature with counters does not trigger the colony")
    void opposingCreatureDoesNotTrigger() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new BuzzardWaspColony());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new DesertersDisciple());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dying.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Deserter's Disciple");
        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each colony receives a full copy of the dying creature's counters")
    void twoColoniesBothReceiveCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BuzzardWaspColony());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BuzzardWaspColony());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new DesertersDisciple());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        dying.setCounterCount(CounterType.FLYING, 1);
        dying.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(first.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    @Test
    @DisplayName("Receiving lethal negative counters can kill the colony")
    void negativeCountersCanKillColony() {
        harness.addToBattlefield(player1, new BuzzardWaspColony());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new DesertersDisciple());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Deserter's Disciple", "Buzzard-Wasp Colony");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature dying with both positive and negative counters transfers both kinds")
    void zeroToughnessDeathPreservesBothCounterKinds() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new BuzzardWaspColony());
        colony.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new DesertersDisciple());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        dying.setCounterCount(CounterType.CHARGE, 2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(colony);
        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(colony.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(colony.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Deserter's Disciple");
    }

    @Test
    @DisplayName("A pending death trigger does not give counters to a different colony")
    void departedSourceDoesNotTransferCountersToAnotherColony() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new BuzzardWaspColony());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new DesertersDisciple());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dying.setMarkedDamage(3);
        harness.runStateBasedActions();

        original.setMarkedDamage(2);
        harness.runStateBasedActions();
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new BuzzardWaspColony());
        harness.passBothPriorities();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(replacement);
        assertThat(gd.stack).isEmpty();
    }
}
