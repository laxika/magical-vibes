package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhoenixWardenOfFire;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoshuaPhoenixsDominant.class, PhoenixWardenOfFire.class, GrizzlyBears.class, Shock.class})
class JoshuaPhoenixsDominantTest extends BaseCardTest {

    @Test
    void mayDiscardNoCardsAndDrawsNone() {
        Shock libraryCard = new Shock();
        GrizzlyBears retainedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new JoshuaPhoenixsDominant(), retainedCard));
        addJoshuaMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void discardsTwoCardsBeforeDrawingTwo() {
        GrizzlyBears firstDiscard = new GrizzlyBears();
        Shock secondDiscard = new Shock();
        Shock retainedCard = new Shock();
        GrizzlyBears firstDraw = new GrizzlyBears();
        Shock secondDraw = new Shock();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new JoshuaPhoenixsDominant(), firstDiscard, secondDiscard, retainedCard));
        addJoshuaMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 3))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDiscard, secondDiscard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard, firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void transformationCannotBeActivatedDuringCombat() {
        addJoshuaReady(player1);
        addJoshuaMana();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, JoshuaPhoenixsDominant.class).isTapped()).isFalse();
    }

    @Test
    void transformationCannotBeActivatedWhileSummoningSick() {
        harness.addToBattlefield(player1, new JoshuaPhoenixsDominant());
        addJoshuaMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chapterTwoDealsDamageAndGainsLifeThroughLifelink() {
        Permanent phoenix = addPhoenixWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(phoenix.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void chapterThreeMayChooseNoTargetsEvenWhenCreaturesAreAvailable() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addPhoenixWithLore(2);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, JoshuaPhoenixsDominant.class).isTransformed()).isFalse();
    }

    @Test
    void returningFrontFaceTriggersDiscardAndDrawAgain() {
        Shock discardedCard = new Shock();
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        addPhoenixWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(findPermanent(player1, JoshuaPhoenixsDominant.class).isTransformed()).isFalse();
    }

    @Test
    void chapterThreeOffersOnlyCreaturesFromControllersGraveyard() {
        GrizzlyBears ownCreature = new GrizzlyBears();
        GrizzlyBears opposingCreature = new GrizzlyBears();
        Shock noncreature = new Shock();
        harness.setGraveyard(player1, List.of(ownCreature, noncreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        addPhoenixWithLore(2);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void transformsAndPhoenixDealsDamageInChapterOne() {
        Permanent joshua = addJoshuaReady(player1);
        addJoshuaMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent phoenix = findPermanent(player1, PhoenixWardenOfFire.class);
        assertThat(phoenix.isTransformed()).isTrue();
        assertThat(phoenix.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(joshua);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void chapterThreeReturnsAnyNumberWithinTotalManaValueAndTransformsBack() {
        List<GrizzlyBears> bears = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, bears.stream().map(card -> (Card) card).toList());
        addPhoenixWithLore(2);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                bears.stream().map(GrizzlyBears::getId).toList());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, bears.stream().map(GrizzlyBears::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, bears.subList(0, 3).stream().map(GrizzlyBears::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears.get(3));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(findPermanent(player1, JoshuaPhoenixsDominant.class).isTransformed()).isFalse();
    }

    @Test
    void chapterThreeStillTransformsBackWhenNoCreatureCardsCanBeReturned() {
        addPhoenixWithLore(2);

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();

        Permanent joshua = findPermanent(player1, JoshuaPhoenixsDominant.class);
        assertThat(joshua.isTransformed()).isFalse();
    }

    private void addJoshuaMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addJoshuaReady(Player player) {
        return addCreatureReady(player, new JoshuaPhoenixsDominant());
    }

    private Permanent addPhoenixWithLore(int loreCounters) {
        JoshuaPhoenixsDominant front = new JoshuaPhoenixsDominant();
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, front);
        phoenix.setCard(front.getBackFaceCard());
        phoenix.setTransformed(true);
        phoenix.setSummoningSick(false);
        phoenix.setCounterCount(CounterType.LORE, loreCounters);
        return phoenix;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Player player, Class<?> cardClass) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> cardClass.isInstance(permanent.getCard()))
                .findFirst()
                .orElseThrow();
    }
}
