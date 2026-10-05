package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CautiousSurvivor;
import com.github.laxika.magicalvibes.cards.l.LiveOrDie;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.u.UnsettlingTwins;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParanormalAnalyst.class, UnsettlingTwins.class, Forest.class,
        CautiousSurvivor.class, LiveOrDie.class, Murder.class})
class ParanormalAnalystTest extends BaseCardTest {

    @Test
    void returnsTheCardPutIntoGraveyardByManifestDread() {
        Card manifestedCard = new Forest();
        Card graveyardCard = new Forest();
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.castFromHand(player1, new UnsettlingTwins(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCard);
    }

    @Test
    void stillTriggersWhenManifestDreadHasNoOtherCardToPutIntoGraveyard() {
        Card manifestedCard = new Forest();
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.castFromHand(player1, new UnsettlingTwins(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void stillTriggersWhenManifestDreadUsesAnEmptyLibrary() {
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new UnsettlingTwins(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void multipleAnalystsReturnTheCardOnlyOnce() {
        Card manifestedCard = new Forest();
        Card graveyardCard = new CautiousSurvivor();
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.castFromHand(player1, new UnsettlingTwins(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCard);
    }

    @Test
    void doesNotReturnTheCardWhenOnlyTheOpponentControlsAnAnalyst() {
        Card manifestedCard = new Forest();
        Card graveyardCard = new CautiousSurvivor();
        harness.addToBattlefield(player2, new ParanormalAnalyst());
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.castFromHand(player1, new UnsettlingTwins(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
    }

    @Test
    void stillReturnsTheCardAfterTheAnalystLeavesTheBattlefield() {
        Card manifestedCard = new Forest();
        Card graveyardCard = new CautiousSurvivor();
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.castFromHand(player1, new UnsettlingTwins(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Paranormal Analyst"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Paranormal Analyst");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCard);
    }

    @Test
    void doesNotReturnACardThatLeftAndReenteredTheGraveyard() {
        Card manifestedCard = new Forest();
        Card graveyardCard = new CautiousSurvivor();
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.castFromHand(player1, new UnsettlingTwins(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        harness.setHand(player1, List.of(new LiveOrDie(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, 0, graveyardCard.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cautious Survivor");
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Cautious Survivor"));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }
}
