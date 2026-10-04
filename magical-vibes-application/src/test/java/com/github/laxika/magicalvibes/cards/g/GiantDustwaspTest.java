package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantDustwasp.class, PithingNeedle.class})
class GiantDustwaspTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Giant Dustwasp with four time counters")
    void suspendExilesWithFourTimeCounters() {
        GiantDustwasp card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        GiantDustwasp card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        var permanent = findPermanent(player1, "Giant Dustwasp");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Giant Dustwasp exiled without time counters")
    void decliningSuspendCastLeavesCardExiled() {
        GiantDustwasp card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotOnBattlefield(player1, "Giant Dustwasp");
        harness.assertNotInGraveyard(player1, "Giant Dustwasp");
    }

    @Test
    @DisplayName("Pithing Needle does not prevent the suspend special action")
    void namedCardCanStillBeSuspended() {
        harness.castFromHand(player1, new PithingNeedle(), "{1}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Giant Dustwasp");

        GiantDustwasp card = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the owner's upkeep removes a suspend counter")
    void opponentUpkeepDoesNotRemoveCounter() {
        GiantDustwasp card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        harness.assertNotOnBattlefield(player1, "Giant Dustwasp");
    }

    @Test
    @DisplayName("Casting normally does not grant suspend haste")
    void normalCastDoesNotGrantHaste() {
        harness.castFromHand(player1, new GiantDustwasp(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Giant Dustwasp"), Keyword.HASTE)).isFalse();
    }

    private GiantDustwasp suspendCard() {
        GiantDustwasp card = new GiantDustwasp();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
