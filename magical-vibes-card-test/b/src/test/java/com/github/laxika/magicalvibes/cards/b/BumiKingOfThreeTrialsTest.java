package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PlanetariumOfWanShiTong;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BumiKingOfThreeTrials.class, AirbendingLesson.class, Forest.class, PlanetariumOfWanShiTong.class})
class BumiKingOfThreeTrialsTest extends BaseCardTest {

    @Test
    void noLessonsMeansNoModesAreChosen() {
        Permanent bumi = castBumi();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void oneLessonAllowsOneMode() {
        Permanent bumi = castBumi(new AirbendingLesson());

        harness.handleListChoice(player1, "Put three +1/+1 counters on Bumi.");
        harness.passBothPriorities();

        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void threeLessonsAllowTwoModesAndEarthbendTheChosenLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bumi = castBumi(new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson());

        harness.handleListChoice(player1, "Put three +1/+1 counters on Bumi.");
        harness.handleListChoice(player1, "Earthbend 3.");
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void mayChooseNoModesEvenWithLessons() {
        Permanent bumi = castBumi(new AirbendingLesson(), new AirbendingLesson());

        harness.handleListChoice(player1, ChooseOneEffect.NO_MODE_LABEL);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentLessonsAndNonLessonCardsDoNotIncreaseModeLimit() {
        harness.setGraveyard(player2, List.of(new AirbendingLesson(), new AirbendingLesson()));
        Permanent bumi = castBumi(new Forest(), new AirbendingLesson());

        harness.handleListChoice(player1, "Put three +1/+1 counters on Bumi.");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void targetedOpponentMakesTheirOwnScryChoices() {
        Card first = new Forest();
        Card second = new AirbendingLesson();
        Card third = new BumiKingOfThreeTrials();
        Card fourth = new Forest();
        harness.setLibrary(player2, List.of(first, second, third, fourth));
        Permanent bumi = castBumi(new AirbendingLesson());

        harness.handleListChoice(player1, "Target player scries 3.");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.libraryOwnerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(2, 0), List.of(1)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third, first, fourth, second);
        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canTargetSelfWithShortLibrary() {
        Card first = new Forest();
        Card second = new AirbendingLesson();
        harness.setLibrary(player1, List.of(first, second));
        castBumi(new AirbendingLesson());

        harness.handleListChoice(player1, "Target player scries 3.");
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    void threeLessonsAllowAllThreeModesWithDistinctTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card top = new Forest();
        harness.setLibrary(player2, List.of(top));
        Permanent bumi = castBumi(new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson());

        harness.handleListChoice(player1, "Put three +1/+1 counters on Bumi.");
        harness.handleListChoice(player1, "Target player scries 3.");
        harness.handleListChoice(player1, "Earthbend 3.");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void earthbendedLandReturnsTappedAfterDying() {
        Permanent land = earthbendLand();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        assertReturnedLand(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land.getCard());
    }

    @Test
    void earthbendedLandReturnsTappedAfterBeingExiled() {
        Permanent land = earthbendLand();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));
        harness.passBothPriorities();

        assertReturnedLand(land);
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    @Test
    void scryingEmptyLibraryStillTriggersScryAbilities() {
        Card planetarium = new PlanetariumOfWanShiTong();
        harness.addToBattlefield(player1, planetarium);
        harness.setLibrary(player1, List.of());
        castBumi(new AirbendingLesson());

        harness.handleListChoice(player1, "Target player scries 3.");
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(planetarium));
    }

    private Permanent earthbendLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBumi(new AirbendingLesson());
        harness.handleListChoice(player1, "Earthbend 3.");
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        return land;
    }

    private void assertReturnedLand(Permanent original) {
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(original.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castBumi(Card... lessons) {
        harness.setGraveyard(player1, List.of(lessons));
        harness.castFromHand(player1, new BumiKingOfThreeTrials(), "{5}{G}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BumiKingOfThreeTrials)
                .findFirst()
                .orElseThrow();
    }
}
