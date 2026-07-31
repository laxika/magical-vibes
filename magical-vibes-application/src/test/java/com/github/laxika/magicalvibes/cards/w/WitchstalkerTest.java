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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
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

    private Permanent getWitchstalker() {
        return findPermanent(player1, "Witchstalker");
    }
}
