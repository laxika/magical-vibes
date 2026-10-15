package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KomaWorldEater;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Refute.class, SavannahLions.class, Forest.class, KomaWorldEater.class})
class RefuteTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell, then draws and discards a card")
    void countersDrawsAndDiscards() {
        SavannahLions lions = new SavannahLions();
        harness.castFromHand(player1, lions, "{W}");

        Refute refute = new Refute();
        harness.setHand(player2, List.of(refute));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        Forest drawnCard = new Forest();
        harness.setLibrary(player2, List.of(drawnCard));

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lions.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lions);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(refute, drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May discard a card already in hand and keep the drawn card")
    void canDiscardAnExistingCard() {
        SavannahLions lions = new SavannahLions();
        harness.castFromHand(player1, lions, "{W}");
        Refute refute = new Refute();
        Forest existingCard = new Forest();
        Forest drawnCard = new Forest();
        harness.setHand(player2, List.of(refute, existingCard));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lions.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(existingCard, drawnCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(existingCard, refute);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lions);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still draws and discards when the targeted spell cannot be countered")
    void lootsWhenTargetCannotBeCountered() {
        KomaWorldEater koma = new KomaWorldEater();
        harness.castFromHand(player1, koma, "{3}{G}{G}{U}{U}");
        Refute refute = new Refute();
        Forest drawnCard = new Forest();
        harness.setHand(player2, List.of(refute));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, koma.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == koma);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(koma);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getCard() == koma);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(refute, drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not draw or discard if its target has left the stack")
    void doesNotLootWithAnIllegalTarget() {
        SavannahLions lions = new SavannahLions();
        harness.castFromHand(player1, lions, "{W}");
        Refute firstRefute = new Refute();
        Forest untouchedCard = new Forest();
        Forest untouchedLibraryCard = new Forest();
        harness.setHand(player2, List.of(firstRefute, untouchedCard));
        harness.setLibrary(player2, List.of(untouchedLibraryCard));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lions.getId());

        Refute secondRefute = new Refute();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(secondRefute));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, lions.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lions, secondRefute, drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstRefute).doesNotContain(untouchedCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(untouchedCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouchedLibraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
