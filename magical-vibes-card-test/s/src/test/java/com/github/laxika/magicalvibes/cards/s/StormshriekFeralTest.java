package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormshriekFeral.class, Plains.class})
class StormshriekFeralTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability boosts Stormshriek Feral until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent feral = addReadyFeral(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(feral.getEffectivePower()).isEqualTo(4);
        assertThat(feral.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(feral.getEffectivePower()).isEqualTo(3);
        assertThat(feral.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Omen discards a card, draws two cards, and shuffles the card into its owner's library")
    void omenDiscardsDrawsAndShuffles() {
        Card discarded = new Plains();
        Card firstDraw = new Plains();
        Card secondDraw = new Plains();
        StormshriekFeral card = new StormshriekFeral();
        harness.setHand(player1, new ArrayList<>(List.of(card, discarded)));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Omen with an empty hand draws nothing but still shuffles itself")
    void omenWithoutDiscardDoesNotDraw() {
        StormshriekFeral card = new StormshriekFeral();
        Card firstDraw = new Plains();
        Card secondDraw = new Plains();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDraw, secondDraw, card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Multiple activations stack even while the creature is summoning sick")
    void repeatedActivationsWhileSummoningSick() {
        Permanent feral = harness.addToBattlefieldAndReturn(player1, new StormshriekFeral());
        feral.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(feral.getEffectivePower()).isEqualTo(5);
        assertThat(feral.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addReadyFeral(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new StormshriekFeral());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
