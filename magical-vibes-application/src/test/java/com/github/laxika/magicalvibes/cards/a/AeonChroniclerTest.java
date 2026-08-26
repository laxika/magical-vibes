package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AeonChronicler.class, GrizzlyBears.class})
class AeonChroniclerTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualCardsInControllerHand() {
        Permanent chronicler = harness.addToBattlefieldAndReturn(player1, new AeonChronicler());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, chronicler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chronicler)).isEqualTo(3);
    }

    @Test
    void suspendUsesChosenXAsTimeCounters() {
        AeonChronicler card = suspendCard(3);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    void suspendXCannotBeZero() {
        AeonChronicler card = new AeonChronicler();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void removingTimeCounterDrawsACard() {
        AeonChronicler card = suspendCard(2);
        int handSizeBeforeUpkeep = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeUpkeep + 1);
    }

    @Test
    void lastTimeCounterDrawsACardAndOffersFreeCast() {
        AeonChronicler card = suspendCard(1);
        int handSizeBeforeUpkeep = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeUpkeep + 1);
    }

    private AeonChronicler suspendCard(int xValue) {
        AeonChronicler card = new AeonChronicler();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 3);
        harness.activateHandAbility(player1, 0, null, xValue);
        return card;
    }
}
