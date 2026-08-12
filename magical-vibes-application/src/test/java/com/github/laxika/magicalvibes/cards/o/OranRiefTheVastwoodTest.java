package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent oldGreenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        oldGreenCreature.setSummoningSick(false);

        Card newGreenCreature = new GrizzlyBears();
        castCreature(player1, newGreenCreature, 2);

        Card opponentGreenCreature = new GrizzlyBears();
        castCreature(player2, opponentGreenCreature, 2);

        Card newNonGreenCreature = new Memnite();
        castCreature(player1, newNonGreenCreature, 0);

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

    private void castCreature(Player player, Card creature, int manaAmount) {
        prepareMainPhase(player);
        harness.setHand(player, List.of(creature));
        harness.addMana(player, ManaColor.GREEN, manaAmount);
        harness.castCreature(player, 0);
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
