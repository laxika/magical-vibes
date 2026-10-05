package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorDrinker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PileOn.class, GrizzlyBears.class, IchorDrinker.class, Island.class, JaceBeleren.class})
class PileOnTest extends BaseCardTest {

    @Test
    void destroysCreatureAndSurveilsTwo() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new PileOn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    void destroysPlaneswalker() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        harness.setHand(player1, List.of(new PileOn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, jace.getId());

        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        harness.assertInGraveyard(player2, "Jace Beleren");
    }

    @Test
    void cannotTargetNoncreatureNonplaneswalkerPermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new PileOn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void convokeCanPayEntireCostWithSummoningSickCreaturesIncludingTheTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new PileOn()));

        harness.castInstantWithConvoke(player1, 0, List.of(first.getId()),
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));

        assertThat(List.of(first, second, third, fourth)).allMatch(Permanent::isTapped);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(second, third, fourth);
        harness.assertInGraveyard(player1, "Ichor Drinker");
        harness.assertInGraveyard(player1, "Pile On");
    }

    @Test
    void convokeWithGreenCreaturePaysGenericManaAlongsideBlackMana() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new PileOn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(convoker.getId()));
        harness.passBothPriorities();

        assertThat(convoker.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotSurveilWhenItsOnlyTargetLeavesTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchorDrinker());
        Card first = new IchorDrinker();
        Card second = new PileOn();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new PileOn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Pile On");
    }

    @Test
    void canKeepBothSurveilledCardsInReverseOrderWithoutLookingAtThirdCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchorDrinker());
        Card first = new IchorDrinker();
        Card second = new PileOn();
        Card third = new IchorDrinker();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new PileOn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third);
    }

    @Test
    void canPutBothSurveilledCardsIntoGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchorDrinker());
        Card first = new IchorDrinker();
        Card second = new PileOn();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new PileOn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    void surveilsOnlyAvailableCardWhenLibraryHasOneCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchorDrinker());
        Card top = new IchorDrinker();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new PileOn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
    }
}
