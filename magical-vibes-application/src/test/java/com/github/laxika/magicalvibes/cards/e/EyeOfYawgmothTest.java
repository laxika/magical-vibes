package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeOfYawgmoth.class, SpinelessThug.class})
class EyeOfYawgmothTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, then puts one card from its power-sized reveal into hand and exiles the rest")
    void sacrificesCreatureAndChoosesFromPowerSizedReveal() {
        Permanent eye = addReadyEye();
        Permanent creature = addCreatureReady(player1, new SpinelessThug());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Card first = new SpinelessThug();
        Card chosen = new SpinelessThug();
        Card third = new SpinelessThug();
        harness.setLibrary(player1, List.of(first, chosen, third));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(eye.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Spineless Thug");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.allCards()).containsExactly(first, chosen, third);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With an empty library, sacrifices the creature without creating a card choice")
    void emptyLibraryCompletesWithoutCardChoice() {
        addReadyEye();
        addCreatureReady(player1, new SpinelessThug());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spineless Thug");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        addReadyEye();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyEye() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfYawgmoth());
        eye.setSummoningSick(false);
        return eye;
    }

}
