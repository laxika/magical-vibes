package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.t.TheWanderer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandTheDreadhorde.class, GrizzlyBears.class, HillGiant.class, HolyDay.class,
        TheWanderer.class, CharmedStray.class})
class CommandTheDreadhordeTest extends BaseCardTest {

    @Test
    void damagesControllerForTotalManaValueAndReturnsCardsUnderTheirControl() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(ownCreature.getId(), opponentCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opponentCreature.getId()));
    }

    @Test
    void onlyLegalTargetsDealDamageAndReturn() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opponentCreature.getId());
    }

    @Test
    void mayChooseNoTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    void cannotTargetNonCreatureNonPlaneswalkerCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), instant));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returningPlaneswalkerDoesNotPreventTheEarlierDamage() {
        Card wanderer = new TheWanderer();
        harness.setGraveyard(player2, List.of(wanderer));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(wanderer.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertOnBattlefield(player1, "The Wanderer");
        harness.assertNotInGraveyard(player2, "The Wanderer");
    }

    @Test
    void preventedDamageDoesNotStopReanimation() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new TheWanderer());
        wanderer.setCounterCount(CounterType.LOYALTY, wanderer.getCard().getLoyalty());
        Card creature = new CharmedStray();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Charmed Stray");
        harness.assertNotInGraveyard(player2, "Charmed Stray");
    }

    @Test
    void simultaneouslyReturnedCreaturesSeeEachOtherEnter() {
        Card first = new CharmedStray();
        Card second = new CharmedStray();
        harness.setGraveyard(player1, List.of(first));
        harness.setGraveyard(player2, List.of(second));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
    }

    @Test
    void targetMovedToHandDoesNotContributeToDamage() {
        Card removed = new CharmedStray();
        Card remaining = new TheWanderer();
        harness.setGraveyard(player1, List.of(removed));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(removed));
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertOnBattlefield(player1, "The Wanderer");
        harness.assertNotOnBattlefield(player1, "Charmed Stray");
        harness.assertInHand(player1, "Charmed Stray");
    }

    @Test
    void allTargetsBecomingIllegalPreventsDamage() {
        Card creature = new CharmedStray();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Command the Dreadhorde");
    }

    @Test
    void mayReturnMoreThanNinetyNineCards() {
        List<Card> creatures = IntStream.range(0, 100)
                .mapToObj(index -> (Card) new GrizzlyBears()).toList();
        harness.setGraveyard(player1, creatures);
        harness.setLife(player1, 300);
        harness.setHand(player1, List.of(new CommandTheDreadhorde()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, creatures.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        harness.assertLife(player1, 100);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(100);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}
