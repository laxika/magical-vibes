package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShadowbornDemon;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObNixilisTheAdversary.class, DoomBlade.class, Forest.class, GrizzlyBears.class,
        ShadowbornDemon.class})
class ObNixilisTheAdversaryTest extends BaseCardTest {

    @Test
    @DisplayName("Casualty X copies Ob Nixilis as a nonlegendary token with X loyalty")
    void casualtyCopiesWithChosenLoyalty() {
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObNixilisTheAdversary()));
        addObMana();

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false,
                casualtyCreature.getId());
        resolveStackEntries(4);

        List<Permanent> obNixilis = findPermanents(player1, "Ob Nixilis, the Adversary");
        assertThat(obNixilis).hasSize(2);
        assertThat(obNixilis).anySatisfy(permanent ->
                assertThat(permanent.getCounterCount(CounterType.LOYALTY)).isEqualTo(3));
        assertThat(obNixilis).anySatisfy(permanent -> {
            assertThat(permanent.getCard().isToken()).isTrue();
            assertThat(permanent.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Casualty X requires a creature with exactly the chosen power")
    void casualtyRequiresExactChosenPower() {
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObNixilisTheAdversary()));
        addObMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false,
                casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power exactly 3");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(casualtyCreature);
    }

    @Test
    @DisplayName("Plus one makes opponents discard or lose life and gains life with a Demon")
    void plusOne() {
        Permanent obNixilis = addReadyOb(player1, 3);
        harness.addToBattlefield(player1, new ShadowbornDemon());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(obNixilis.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Minus two creates a Devil whose death trigger deals damage")
    void minusTwoCreatesDevil() {
        addReadyOb(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent devil = findPermanents(player1, "Devil").getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, devil.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Minus seven makes a target player draw seven and lose seven life")
    void minusSeven() {
        addReadyOb(player1, 7);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
    }

    private void addObMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyOb(Player player, int loyalty) {
        Permanent permanent = new Permanent(new ObNixilisTheAdversary());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void resolveStackEntries(int count) {
        for (int i = 0; i < count; i++) {
            harness.passBothPriorities();
        }
    }
}
