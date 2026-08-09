package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EyeOfYawgmothTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, then puts one card from its power-sized reveal into hand and exiles the rest")
    void sacrificesCreatureAndChoosesFromPowerSizedReveal() {
        Permanent eye = addReadyEye();
        Permanent creature = addCreatureReady(new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Card first = new GrizzlyBears();
        Card chosen = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, chosen, third));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(eye.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
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

    private Permanent addCreatureReady(Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        creature.setSummoningSick(false);
        return creature;
    }
}
