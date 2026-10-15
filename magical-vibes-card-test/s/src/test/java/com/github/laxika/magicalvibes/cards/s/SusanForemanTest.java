package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SusanForeman.class, Panopticon.class})
class SusanForemanTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Panopticon(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void ordersTopTwoPlanarCardsBeforePlaneswalking() {
        harness.addToBattlefield(player1, new SusanForeman());
        Card departing = gd.planechase.faceUp.getFirst().getCard();
        Card bottom = new Panopticon();
        Card top = new Panopticon();
        gd.planechase.deck.add(bottom);
        gd.planechase.deck.add(top);

        harness.inMutationScope(() -> planar.planeswalk(gd));

        PendingInteraction.PlanarCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PlanarCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.planeswalkAfterChoice()).isTrue();
        assertThat(choice.revealedCards()).containsExactly(bottom, top);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardsChosen(List.of(bottom.getId())));

        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(top);
        assertThat(gd.planechase.deck).containsExactly(bottom, departing);
    }

    @Test
    void tapsForGreenMana() {
        Permanent susan = harness.addToBattlefieldAndReturn(player1, new SusanForeman());
        susan.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(com.github.laxika.magicalvibes.model.ManaColor.GREEN))
                .isEqualTo(1);
    }

    @Test
    void lookingAtPlanarCardsDoesNotRevealThemInThePublicLog() {
        harness.addToBattlefield(player1, new SusanForeman());
        gd.planechase.deck.add(new Panopticon());
        gd.planechase.deck.add(new Panopticon());
        gd.gameLog.clear();

        harness.inMutationScope(() -> planar.planeswalk(gd));

        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("Panopticon"));
    }

    @Test
    void canPutTheSecondCardOnBottomWithoutReorderingTheRestOfTheDeck() {
        harness.addToBattlefield(player1, new SusanForeman());
        Card departing = gd.planechase.faceUp.getFirst().getCard();
        Card first = new Panopticon();
        Card second = new Panopticon();
        Card third = new Panopticon();
        gd.planechase.deck.addAll(List.of(first, second, third));

        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(first);
        assertThat(gd.planechase.deck).containsExactly(third, second, departing);
    }

    @Test
    void doesNotReplaceAnOpponentsPlaneswalk() {
        harness.addToBattlefield(player2, new SusanForeman());
        Card first = new Panopticon();
        Card second = new Panopticon();
        gd.planechase.deck.addAll(List.of(first, second));

        harness.inMutationScope(() -> planar.planeswalk(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PlanarCardChoice.class)).isNull();
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isSameAs(first);
    }

    @Test
    void cannotTapForManaWhileSummoningSick() {
        Permanent susan = harness.addToBattlefieldAndReturn(player1, new SusanForeman());
        susan.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(susan.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(com.github.laxika.magicalvibes.model.ManaColor.GREEN))
                .isZero();
    }
}
