package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritMascot.class, Disentomb.class, GrizzlyBears.class, Reminisce.class, Shock.class, TormodsCrypt.class})
class SpiritMascotTest extends BaseCardTest {

    @Test
    void putsCounterOnItselfWhenCardLeavesOwnGraveyard() {
        Permanent mascot = addCreatureReady(player1, new SpiritMascot());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, card.getId());
        harness.passBothPriorities();

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsOnlyOneCounterWhenSeveralCardsLeaveTogether() {
        Permanent mascot = addCreatureReady(player1, new SpiritMascot());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock(), new Shock())));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenCardEntersOwnGraveyard() {
        Permanent mascot = addCreatureReady(player1, new SpiritMascot());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenCardsLeaveOpponentsGraveyard() {
        Permanent mascot = addCreatureReady(player1, new SpiritMascot());
        harness.setGraveyard(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenEmptyGraveyardIsShuffled() {
        Permanent mascot = addCreatureReady(player1, new SpiritMascot());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersSeparatelyForTwoDeparturesInTheSameTurn() {
        Permanent mascot = addCreatureReady(player1, new SpiritMascot());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Disentomb(), new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        resolveAllTriggers();
        harness.castAndResolveSorcery(player1, 0, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void eachMascotGetsItsOwnCounterAfterTheTriggerResolves() {
        Permanent firstMascot = addCreatureReady(player1, new SpiritMascot());
        Permanent secondMascot = addCreatureReady(player1, new SpiritMascot());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, card.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(firstMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveAllTriggers();

        assertThat(firstMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsOneCounterWhenOpponentExilesSeveralCardsFromItsControllersGraveyard() {
        Permanent mascot = addCreatureReady(player1, new SpiritMascot());
        Card first = new Shock();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addToBattlefield(player2, new TormodsCrypt());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.stack).hasSize(1);
        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveAllTriggers();

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void removedMascotDoesNotPutCounterOnAnotherMascot() {
        Permanent mascot = addCreatureReady(player1, new SpiritMascot());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, card.getId());
        harness.castAndResolveInstant(player2, 0, mascot.getId());
        harness.assertNotOnBattlefield(player1, "Spirit Mascot");
        Permanent otherMascot = addCreatureReady(player1, new SpiritMascot());

        resolveAllTriggers();

        assertThat(otherMascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
