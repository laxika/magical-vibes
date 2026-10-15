package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.n.NissasChosen;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OranRiefTheVastwood.class, NissasChosen.class, StoneworkPuma.class})
class OranRiefTheVastwoodTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for one green mana")
    void entersTappedAndProducesGreenMana() {
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new OranRiefTheVastwood()));

        harness.playLand(player1, 0);

        Permanent oranRief = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(oranRief.isTapped()).isTrue();

        oranRief.untap();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts counters on all green creatures that entered this turn")
    void putsCountersOnAllGreenCreaturesThatEnteredThisTurn() {
        Permanent oranRief = harness.addToBattlefieldAndReturn(player1, new OranRiefTheVastwood());
        oranRief.setSummoningSick(false);
        Permanent oldGreenCreature = harness.addToBattlefieldAndReturn(player1, new NissasChosen());
        oldGreenCreature.setSummoningSick(false);

        Card newGreenCreature = new NissasChosen();
        castCreature(player1, newGreenCreature, "{G}{G}");

        Card opponentGreenCreature = new NissasChosen();
        castCreature(player2, opponentGreenCreature, "{G}{G}");

        Card newNonGreenCreature = new StoneworkPuma();
        castCreature(player1, newNonGreenCreature, "{3}");

        prepareMainPhase(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(oldGreenCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, newGreenCreature)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player2, opponentGreenCreature)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, newNonGreenCreature)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Includes a green creature that enters after activation but before resolution")
    void includesCreatureEnteringInResponse() {
        prepareMainPhase(player1);
        Permanent oranRief = harness.addToBattlefieldAndReturn(player1, new OranRiefTheVastwood());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(oranRief.isTapped()).isTrue();
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new NissasChosen());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each activation can put another counter on the same creature during its entry turn")
    void separateLandsEachPutACounterOnTheSameCreature() {
        prepareMainPhase(player1);
        harness.addToBattlefield(player1, new OranRiefTheVastwood());
        harness.addToBattlefield(player1, new OranRiefTheVastwood());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new NissasChosen());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature that entered on a previous turn no longer receives counters")
    void excludesCreatureAfterTurnChanges() {
        prepareMainPhase(player1);
        harness.addToBattlefield(player1, new OranRiefTheVastwood());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new NissasChosen());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCreature(Player player, Card creature, String manaCost) {
        prepareMainPhase(player);
        harness.castFromHand(player, creature, manaCost);
        harness.passBothPriorities();
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent findPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
