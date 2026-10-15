package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EarthbendingLesson;
import com.github.laxika.magicalvibes.cards.f.FirebendingLesson;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TophHardheadedTeacher.class, Shock.class, GrizzlyBears.class,
        Forest.class, FirebendingLesson.class, HillGiant.class, EarthbendingLesson.class, HardenedScales.class})
class TophHardheadedTeacherTest extends BaseCardTest {

    @Test
    void entersMayDiscardToReturnInstantOrSorcery() {
        Card shock = new Shock();
        Card discard = new GrizzlyBears();
        harness.setHand(player1, List.of(discard));
        harness.setGraveyard(player1, List.of(shock));

        harness.enterBattlefieldAndReturn(player1, new TophHardheadedTeacher());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
    }

    @Test
    void castingNonLessonEarthbendsOne() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new TophHardheadedTeacher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    void castingLessonEarthbendsWithAdditionalCounter() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new TophHardheadedTeacher());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    void cannotDiscardWhenNoGraveyardTargetExists() {
        Card lesson = new FirebendingLesson();
        harness.setHand(player1, List.of(lesson));
        harness.setGraveyard(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new TophHardheadedTeacher());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void graveyardTargetIsChosenBeforeDiscardAndExcludesOtherCards() {
        Card target = new EarthbendingLesson();
        Card creature = new TophHardheadedTeacher();
        Card opponentInstant = new FirebendingLesson();
        Card discard = new FirebendingLesson();
        harness.setHand(player1, List.of(discard));
        harness.setGraveyard(player1, List.of(target, creature));
        harness.setGraveyard(player2, List.of(opponentInstant));

        harness.enterBattlefieldAndReturn(player1, new TophHardheadedTeacher());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(target);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, discard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentInstant);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineDiscardWithoutReturningTarget() {
        Card target = new FirebendingLesson();
        Card handCard = new TophHardheadedTeacher();
        harness.setHand(player1, List.of(handCard));
        harness.setGraveyard(player1, List.of(target));

        harness.enterBattlefieldAndReturn(player1, new TophHardheadedTeacher());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void illegalGraveyardTargetPreventsDiscard() {
        Card target = new FirebendingLesson();
        Card handCard = new TophHardheadedTeacher();
        harness.setHand(player1, List.of(handCard));
        harness.setGraveyard(player1, List.of(target));

        harness.enterBattlefieldAndReturn(player1, new TophHardheadedTeacher());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lessonPlacesCountersInTwoSeparateEventsWithHardenedScales() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent teacher = harness.addToBattlefieldAndReturn(player1, new TophHardheadedTeacher());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, teacher.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void onlyControlledLandsCanBeEarthbent() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent teacher = harness.addToBattlefieldAndReturn(player1, new TophHardheadedTeacher());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, teacher.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownLand.getId());
        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();
        assertThat(ownLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentsSpellDoesNotEarthbend() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent teacher = harness.addToBattlefieldAndReturn(player1, new TophHardheadedTeacher());
        harness.setHand(player2, List.of(new FirebendingLesson()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, teacher.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void repeatedEarthbendingAccumulatesCountersAndDeadLandReturnsTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card landCard = land.getCard();
        harness.addToBattlefield(player1, new TophHardheadedTeacher());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(landCard.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(landCard);
    }
}
