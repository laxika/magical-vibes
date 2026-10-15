package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KotisSibsigChampion.class, GrizzlyBears.class, Zombify.class})
class KotisSibsigChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a creature from the graveyard by exiling three other cards")
    void castsCreatureFromGraveyardAndPutsCountersOnKotis() {
        Permanent kotis = harness.addToBattlefieldAndReturn(player1, new KotisSibsigChampion());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(kotis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The graveyard cast is available only once during each turn")
    void graveyardCastIsOncePerTurn() {
        harness.addToBattlefield(player1, new KotisSibsigChampion());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature returned from the graveyard puts two counters on Kotis")
    void reanimatedCreaturePutsCountersOnKotis() {
        Permanent kotis = harness.addToBattlefieldAndReturn(player1, new KotisSibsigChampion());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(kotis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An ordinary creature entering does not trigger Kotis")
    void ordinaryCreatureDoesNotTrigger() {
        Permanent kotis = harness.addToBattlefieldAndReturn(player1, new KotisSibsigChampion());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(kotis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Kotis triggers when it is itself returned from a graveyard")
    void reanimatedKotisTriggersForItself() {
        Card kotisCard = new KotisSibsigChampion();
        harness.setGraveyard(player1, List.of(kotisCard));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castAndResolveSorcery(player1, 0, kotisCard.getId());
        harness.passBothPriorities();

        Permanent kotis = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(kotisCard.getId()))
                .findFirst().orElseThrow();
        assertThat(kotis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The spell cannot count itself among the three other cards")
    void requiresThreeOtherCards() {
        harness.addToBattlefield(player1, new KotisSibsigChampion());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Kotis does not grant permission to cast noncreature spells")
    void cannotCastNoncreatureSpell() {
        harness.addToBattlefield(player1, new KotisSibsigChampion());
        harness.setGraveyard(player1, List.of(
                new Zombify(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's reanimated creature does not trigger Kotis")
    void opponentReanimationDoesNotTrigger() {
        Permanent kotis = harness.addToBattlefieldAndReturn(player1, new KotisSibsigChampion());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player2, List.of(new Zombify()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(kotis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
