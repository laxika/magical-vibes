package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElspethConquersDeath.class, ChandraNalaar.class, GrizzlyBears.class, HillGiant.class, Opt.class})
class ElspethConquersDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles an opponent's permanent with mana value 3 or greater")
    void chapterIExilesEligibleOpponentPermanent() {
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSaga();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentGiant.getId())
                .doesNotContain(ownGiant.getId(), opponentBears.getId());

        harness.handlePermanentChosen(player1, opponentGiant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentGiant);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentGiant.getCard());
    }

    @Test
    @DisplayName("Chapter II taxes opponents' noncreature spells by two until your next turn")
    void chapterIITaxesOpponentsNoncreatureSpells() {
        Permanent saga = addSagaWithLore(1);
        triggerNextChapter();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(saga);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Chapter III returns a creature with a plus-one-plus-one counter")
    void chapterIIIReturnsCreatureWithCounter() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        Permanent saga = addSagaWithLore(2);
        harness.setGraveyard(player1, List.of(creatureCard));

        triggerNextChapter();
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creatureCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a +1/+1 counter on it");

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(saga).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Chapter III returns a planeswalker with a loyalty counter")
    void chapterIIIReturnsPlaneswalkerWithLoyaltyCounter() {
        ChandraNalaar planeswalkerCard = new ChandraNalaar();
        addSagaWithLore(2);
        harness.setGraveyard(player1, List.of(planeswalkerCard));

        triggerNextChapter();
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(planeswalkerCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(planeswalkerCard.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a loyalty counter on it");

        Permanent returned = findPermanent(player1, "Chandra Nalaar");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castSaga() {
        harness.setHand(player1, List.of(new ElspethConquersDeath()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ElspethConquersDeath());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
