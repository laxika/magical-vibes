package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViviensInvocation.class, ColossalDreadmaw.class, GreenwoodSentinel.class, Shock.class})
class ViviensInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a chosen creature from the top seven onto the battlefield and it deals its power to the target")
    void putsCreatureAndDealsPowerDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Card chosen = new GreenwoodSentinel();
        setLibrary(chosen, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        castInvocation();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosen.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == chosen)
                .findFirst()
                .orElseThrow();
        assertThat(entered.getMarkedDamage()).isEqualTo(0);
        assertThat(target.getMarkedDamage()).isEqualTo(0);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getMarkedDamage()).isEqualTo(0);
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6).doesNotContain(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May decline to put a creature onto the battlefield")
    void mayDeclineCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Card chosen = new GreenwoodSentinel();
        setLibrary(chosen, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        castInvocation();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() == chosen);
        assertThat(target.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can put a creature onto the battlefield when no opponent controls a creature")
    void putsCreatureWithoutAnyLegalDamageTarget() {
        Card chosen = new GreenwoodSentinel();
        setLibrary(chosen);
        castInvocation();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == chosen);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Leaves cards below the top seven above the randomly bottomed cards")
    void onlyLooksAtTopSeven() {
        Card chosen = new GreenwoodSentinel();
        Card untouched = new ColossalDreadmaw();
        List<Card> unchosen = List.of(new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, List.of(chosen, unchosen.get(0), unchosen.get(1),
                unchosen.get(2), unchosen.get(3), unchosen.get(4), unchosen.get(5), untouched));
        castInvocation();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7).startsWith(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(unchosen);
    }

    @Test
    @DisplayName("A library with no creature cards is bottomed without creating a damage trigger")
    void noCreatureAmongLookedAtCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        List<Card> cards = List.of(new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, cards);
        castInvocation();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(target.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The damage trigger uses last known power if the entering creature dies in response")
    void dealsDamageAfterSourceLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Card chosen = new GreenwoodSentinel();
        setLibrary(chosen);
        castInvocation();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == chosen).findFirst().orElseThrow();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, entered.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chosen);
        assertThat(target.getMarkedDamage()).isEqualTo(0);
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    private void castInvocation() {
        harness.setHand(player1, List.of(new ViviensInvocation()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
