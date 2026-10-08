package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YawgmothsWill;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionsOfDominance.class, GrizzlyBears.class, EdgarMarkov.class, YawgmothsWill.class})
class VisionsOfDominanceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the target creature, then doubles its +1/+1 counters")
    void addsAndDoublesPlusOneCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player1, List.of(new VisionsOfDominance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flashback uses the greatest mana value of an owned commander as a cost reduction")
    void flashbackUsesCommanderReduction() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        VisionsOfDominance spell = new VisionsOfDominance();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("A creature with no counters receives two +1/+1 counters")
    void startsWithNoCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new VisionsOfDominance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Visions of Dominance");
    }

    @Test
    @DisplayName("Flashback without a commander costs the full eight generic and two green mana")
    void flashbackWithoutCommander() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EdgarMarkov());
        VisionsOfDominance spell = new VisionsOfDominance();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("An owned commander on an opponent's battlefield still reduces flashback cost")
    void ownedCommanderControlledByOpponent() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        Permanent target = harness.addToBattlefieldAndReturn(player1, commander);
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.setGraveyard(player1, List.of(new VisionsOfDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An owned commander in the graveyard does not reduce flashback cost")
    void commanderInGraveyardDoesNotReduceCost() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EdgarMarkov());
        harness.setGraveyard(player1, List.of(new VisionsOfDominance(), commander));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Commander reduction does not apply when casting from hand")
    void handCastDoesNotReceiveCommanderReduction() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        Permanent target = harness.addToBattlefieldAndReturn(player1, commander);
        harness.setHand(player1, List.of(new VisionsOfDominance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's commander does not reduce flashback cost")
    void opponentsCommanderDoesNotReduceCost() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player2.getId(), commander);
        Permanent target = harness.addToBattlefieldAndReturn(player2, commander);
        harness.setGraveyard(player1, List.of(new VisionsOfDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Commander reduction cannot pay the green portion of the flashback cost")
    void reductionDoesNotReduceColoredMana() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        Permanent target = harness.addToBattlefieldAndReturn(player1, commander);
        harness.setGraveyard(player1, List.of(new VisionsOfDominance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting through Yawgmoth's Will at the normal mana cost does not receive flashback reduction")
    void normalGraveyardCastDoesNotReceiveFlashbackReduction() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        Permanent target = harness.addToBattlefieldAndReturn(player1, commander);
        harness.setGraveyard(player1, List.of(new VisionsOfDominance()));
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
