package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MazemindTome.class, ReturnToNature.class})
class MazemindTomeTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability puts a page counter on Mazemind Tome and scries 1")
    void firstAbilityScries() {
        Permanent tome = addTome();
        List<Card> library = gd.playerDecks.get(player1.getId());
        Card top = library.getFirst();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(library.getFirst()).isSameAs(top);
    }

    @Test
    @DisplayName("The second ability puts a page counter on Mazemind Tome and draws a card")
    void secondAbilityDraws() {
        Permanent tome = addTome();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Mazemind Tome exiles itself and its controller gains 4 life at four page counters")
    void exilesAndGainsLifeAtFourPageCounters() {
        Permanent tome = addTome();
        tome.setCounterCount(CounterType.PAGE, 3);
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mazemind Tome")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("The life gain does not happen if Mazemind Tome leaves before its trigger resolves")
    void lifeGainRequiresSuccessfulExile() {
        Permanent tome = addTome();
        tome.setCounterCount(CounterType.PAGE, 3);
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, tome.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mazemind Tome")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom without exiling Tome below four counters")
    void scryPutsCardOnBottomBelowThreshold() {
        Permanent tome = addTome();
        tome.setCounterCount(CounterType.PAGE, 2);
        List<Card> library = gd.playerDecks.get(player1.getId());
        Card top = library.getFirst();
        Card next = library.get(1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(library.getFirst()).isSameAs(next);
        assertThat(library.getLast()).isSameAs(top);
        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(3);
        assertThat(tome.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Mazemind Tome")).containsExactly(tome);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The fourth page counter exiles Tome before drawing, but the draw still resolves")
    void exileResolvesBeforeFourthDraw() {
        Permanent tome = addTome();
        tome.setCounterCount(CounterType.PAGE, 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mazemind Tome")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Scry still resolves after Tome is exiled with more than four counters")
    void scryResolvesAfterExileAboveThreshold() {
        Permanent tome = addTome();
        tome.setCounterCount(CounterType.PAGE, 4);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mazemind Tome")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private Permanent addTome() {
        return harness.addToBattlefieldAndReturn(player1, new MazemindTome());
    }
}
