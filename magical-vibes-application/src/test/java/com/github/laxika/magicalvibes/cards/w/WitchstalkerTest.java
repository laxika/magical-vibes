package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Witchstalker.class, GrizzlyBears.class, Shock.class, Unsummon.class, DoomBlade.class})
class WitchstalkerTest extends BaseCardTest {

    private UUID setUpControllerTurn() {
        harness.addToBattlefield(player1, new Witchstalker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.getPermanentId(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent's blue spell during your turn puts a +1/+1 counter on Witchstalker")
    void opponentBlueSpellOnYourTurnAddsCounter() {
        UUID bearsId = setUpControllerTurn();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        Permanent stalker = getWitchstalker();
        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castInstant(player2, 0, bearsId);

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Witchstalker"))
                .count()).isEqualTo(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's black spell during your turn puts a +1/+1 counter on Witchstalker")
    void opponentBlackSpellOnYourTurnAddsCounter() {
        UUID bearsId = setUpControllerTurn();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        Permanent stalker = getWitchstalker();

        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's non-blue/black spell during your turn does not trigger")
    void opponentRedSpellDoesNotTrigger() {
        UUID bearsId = setUpControllerTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        Permanent stalker = getWitchstalker();

        harness.castInstant(player2, 0, bearsId);

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent's blue spell during the opponent's turn does not trigger")
    void blueSpellOnOpponentTurnDoesNotTrigger() {
        harness.addToBattlefield(player1, new Witchstalker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        Permanent stalker = getWitchstalker();

        harness.castInstant(player2, 0, bearsId);

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Your own blue spell does not trigger")
    void ownBlueSpellDoesNotTrigger() {
        UUID bearsId = setUpControllerTurn();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent stalker = getWitchstalker();

        harness.castInstant(player1, 0, bearsId);

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentCannotTargetWitchstalker() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new Witchstalker());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, stalker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Witchstalker");
    }

    @Test
    void controllerCanTargetWitchstalkerWithoutTriggeringIt() {
        setUpControllerTurn();
        Permanent stalker = getWitchstalker();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, stalker.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Witchstalker");
        harness.assertInHand(player1, "Witchstalker");
        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachWitchstalkerGetsItsOwnCounter() {
        UUID bearsId = setUpControllerTurn();
        Permanent first = getWitchstalker();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Witchstalker());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, bearsId);
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void eachQualifyingSpellTriggersDuringCombat() {
        setUpControllerTurn();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        Permanent stalker = getWitchstalker();
        harness.setHand(player2, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        for (int i = 0; i < 2; i++) {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.castAndResolveInstant(player2, 0, target.getId());
            assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(i + 1);
            harness.passBothPriorities();
        }
    }

    @Test
    void triggerStillResolvesWhenTriggeringSpellLosesItsTarget() {
        UUID bearsId = setUpControllerTurn();
        Permanent stalker = getWitchstalker();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Unsummon");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    private Permanent getWitchstalker() {
        return findPermanent(player1, "Witchstalker");
    }
}
