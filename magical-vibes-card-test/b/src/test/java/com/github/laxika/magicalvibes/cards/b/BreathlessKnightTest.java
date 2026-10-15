package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WorldheartPhoenix;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreathlessKnight.class, GrizzlyBears.class, WorldheartPhoenix.class, Zombify.class})
class BreathlessKnightTest extends BaseCardTest {

    @Test
    void putsCounterOnItselfWhenItEntersFromGraveyard() {
        Card knightCard = new BreathlessKnight();
        harness.setGraveyard(player1, List.of(knightCard));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, knightCard.getId());
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Breathless Knight");
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnItselfWhenAnotherCreatureEntersFromGraveyard() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BreathlessKnight());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnItselfWhenCreatureIsCastFromGraveyard() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BreathlessKnight());
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForAnOrdinaryCreatureEntering() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BreathlessKnight());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenItIsCastFromHand() {
        harness.setHand(player1, List.of(new BreathlessKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Breathless Knight")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenAnOpponentReanimatesACreature() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BreathlessKnight());
        Card otherKnight = new BreathlessKnight();
        harness.setGraveyard(player2, List.of(otherKnight));
        harness.setHand(player2, List.of(new Zombify()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, otherKnight.getId());
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Breathless Knight")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachKnightGetsOneCounterWhenAnotherKnightIsReanimated() {
        Permanent existingKnight = harness.addToBattlefieldAndReturn(player1, new BreathlessKnight());
        Card returningKnight = new BreathlessKnight();
        harness.setGraveyard(player1, List.of(returningKnight));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, returningKnight.getId());
        resolveAllTriggers();

        assertThat(existingKnight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Breathless Knight"))
                .hasSize(2)
                .allSatisfy(knight -> assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
    }

    @Test
    void graveyardCastTriggersOnlyAfterTheCreatureEntersAndTheCounterUsesTheStack() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BreathlessKnight());
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyard(player1, 0);

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Worldheart Phoenix");
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
