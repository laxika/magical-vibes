package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrashingFootfalls.class, PithingNeedle.class})
class CrashingFootfallsTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Crashing Footfalls with four time counters")
    void suspendExilesWithFourTimeCounters() {
        CrashingFootfalls card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and creates two trample Rhinos")
    void lastCounterOffersFreeCastAndCreatesTwoRhinos() {
        CrashingFootfalls card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Rhino");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
            assertThat(token.getCard().getPower()).isEqualTo(4);
            assertThat(token.getCard().getToughness()).isEqualTo(4);
            assertThat(token.getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.RHINO);
            assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        });
        harness.assertInGraveyard(player1, "Crashing Footfalls");
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove a suspend counter")
    void opponentsUpkeepDoesNotRemoveCounter() {
        CrashingFootfalls card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each owner upkeep removes exactly one time counter")
    void eachUpkeepRemovesOneCounter() {
        CrashingFootfalls card = suspendCard();

        for (int remaining = 3; remaining > 0; remaining--) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), remaining);
            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
            assertThat(findPermanents(player1, "Rhino")).isEmpty();
        }
    }

    @Test
    @DisplayName("Declining the suspend cast leaves the card exiled without another offer")
    void decliningFreeCastLeavesCardExiled() {
        CrashingFootfalls card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rhino")).isEmpty();
    }

    @Test
    @DisplayName("Pithing Needle cannot prevent the suspend special action")
    void pithingNeedleDoesNotPreventSuspending() {
        Permanent needle = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        needle.setChosenName("Crashing Footfalls");

        CrashingFootfalls card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    private CrashingFootfalls suspendCard() {
        CrashingFootfalls card = new CrashingFootfalls();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
