package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfTheGiants.class, ChandraNalaar.class, Forest.class, HillGiant.class, Shock.class})
class InvasionOfTheGiantsTest extends BaseCardTest {

    @Test
    void chapterIScriesTwo() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new InvasionOfTheGiants());
        saga.setCounterCount(CounterType.LORE, 0);
        harness.setLibrary(player1, List.of(new Shock(), new Forest()));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void chapterIIDrawsAndMayRevealAGiantToDamageAnOpponent() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new InvasionOfTheGiants());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new HillGiant()));

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .anyMatch(card -> card.getSubtypes().contains(CardSubtype.GIANT));
    }

    @Test
    void chapterIIMayDamageAPlaneswalker() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new InvasionOfTheGiants());
        saga.setCounterCount(CounterType.LORE, 1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new HillGiant()));

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void chapterIIIReducesTheNextGiantSpellOnly() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new InvasionOfTheGiants());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setHand(player1, List.of(new HillGiant()));

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.GIANT));
        assertThat(gd.floatingEffects).isEmpty();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
