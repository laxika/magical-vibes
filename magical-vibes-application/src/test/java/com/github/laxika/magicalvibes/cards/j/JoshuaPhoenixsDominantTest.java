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
        Permanent joshua = new Permanent(new JoshuaPhoenixsDominant());
        joshua.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(joshua);
        return joshua;
    }

    private Permanent addPhoenixWithLore(int loreCounters) {
        JoshuaPhoenixsDominant front = new JoshuaPhoenixsDominant();
        Permanent phoenix = new Permanent(front);
        phoenix.setCard(front.getBackFaceCard());
        phoenix.setTransformed(true);
        phoenix.setSummoningSick(false);
        phoenix.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(phoenix);
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
