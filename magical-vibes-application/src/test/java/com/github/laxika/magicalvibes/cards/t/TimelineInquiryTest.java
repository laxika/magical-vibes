package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JarvisEarthsMightiestButler;
import com.github.laxika.magicalvibes.cards.q.QuinjetTechnician;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimelineInquiry.class, GrizzlyBears.class, JarvisEarthsMightiestButler.class,
        QuinjetTechnician.class})
class TimelineInquiryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and then discards one without teamwork")
    void drawsThreeThenDiscardsWithoutTeamwork() {
        Card discardedCard = new GrizzlyBears();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        Card thirdDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(new TimelineInquiry(), discardedCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }

    @Test
    @DisplayName("Teamwork draws three cards without discarding and taps the chosen creatures")
    void teamworkDrawsThreeWithoutDiscarding() {
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        Card thirdDraw = new GrizzlyBears();
        Card spell = new TimelineInquiry();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        Permanent teamworkCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of(teamworkCreature.getId()));
        harness.passBothPriorities();

        assertThat(teamworkCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void canDiscardANewlyDrawnCardEvenWhenTeamworkIsAvailable() {
        Card spell = new TimelineInquiry();
        Card firstDraw = new TimelineInquiry();
        Card secondDraw = new TimelineInquiry();
        Card thirdDraw = new TimelineInquiry();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QuinjetTechnician());
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(spell, firstDraw);
    }

    @Test
    void canTapMultipleSummoningSickCreaturesWithMoreThanRequiredPower() {
        Card spell = new TimelineInquiry();
        Card firstDraw = new TimelineInquiry();
        Card secondDraw = new TimelineInquiry();
        Card thirdDraw = new TimelineInquiry();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new JarvisEarthsMightiestButler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new QuinjetTechnician());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        addMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of(first.getId(), second.getId()));
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
    }

    @Test
    void cannotPayTeamworkWithInsufficientPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JarvisEarthsMightiestButler());
        Card spell = new TimelineInquiry();
        harness.setHand(player1, List.of(spell));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(
                player1, 0, null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayTeamworkWithAnAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QuinjetTechnician());
        creature.tap();
        Card spell = new TimelineInquiry();
        harness.setHand(player1, List.of(spell));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(
                player1, 0, null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayTeamworkWithAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new QuinjetTechnician());
        Card spell = new TimelineInquiry();
        harness.setHand(player1, List.of(spell));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(
                player1, 0, null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCountTheSameCreatureTwiceForTeamwork() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JarvisEarthsMightiestButler());
        Card spell = new TimelineInquiry();
        harness.setHand(player1, List.of(spell));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(
                player1, 0, null, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
