package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HarshDeceiverTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability looks at the top card and leaves it on top")
    void looksAtTopCard() {
        Permanent deceiver = addReadyDeceiver(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(topCard);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(deceiver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing a land untaps Harsh Deceiver and gives it +1/+1")
    void landRevealUntapsAndBoosts() {
        Permanent deceiver = addReadyDeceiver(player1);
        deceiver.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(deceiver.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Revealing a nonland card does not untap or boost Harsh Deceiver")
    void nonlandRevealDoesNothing() {
        Permanent deceiver = addReadyDeceiver(player1);
        deceiver.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(deceiver.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("The reveal ability can only be activated once each turn")
    void revealAbilityOnlyOnceEachTurn() {
        addReadyDeceiver(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDeceiver(Player player) {
        Permanent deceiver = new Permanent(new HarshDeceiver());
        deceiver.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(deceiver);
        return deceiver;
    }
}
