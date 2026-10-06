package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GoblinAnarchomancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaidDone.class, Divination.class, GrizzlyBears.class, HolyDay.class,
        Counterspell.class, GoblinAnarchomancer.class})
class SaidDoneTest extends BaseCardTest {

    @Test
    void saidReturnsAnInstantOrSorceryFromTheGraveyard() {
        Card instant = new HolyDay();
        Card sorcery = new Divination();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(instant, sorcery, creature));
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void saidExcludesCreatureCardsFromTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doneTapsUpToTwoCreaturesAndSkipsTheirNextUntap() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isFalse();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
        assertThat(third.getSkipUntapCount()).isZero();
    }

    @Test
    void doneCanBeCastDuringTheOpponentsUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinAnarchomancer());
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void saidCannotBeCastDuringTheOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(new Counterspell()));
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void saidReturnsAnInstantAndDoesNotOfferTheOpponentsCards() {
        Card ownInstant = new Counterspell();
        Card opposingInstant = new Counterspell();
        harness.setGraveyard(player1, List.of(ownInstant));
        harness.setGraveyard(player2, List.of(opposingInstant));
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownInstant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownInstant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Counterspell");
        harness.assertNotInGraveyard(player1, "Counterspell");
        harness.assertInGraveyard(player2, "Counterspell");
    }

    @Test
    void saidRequiresOneGraveyardTargetRatherThanAllowingZero() {
        Card instant = new Counterspell();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Counterspell");
    }

    @Test
    void saidDoesNotReturnATargetThatLeftTheGraveyardBeforeResolution() {
        Card instant = new Counterspell();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Counterspell");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doneCanResolveWithoutTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinAnarchomancer());
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void doneLocksAnAlreadyTappedCreatureForOnlyItsControllersNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinAnarchomancer());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void doneRejectsMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GoblinAnarchomancer());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GoblinAnarchomancer());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GoblinAnarchomancer());
        harness.setHand(player1, List.of(new SaidDone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
