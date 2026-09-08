package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonlordOjutai.class, GrizzlyBears.class, Shock.class})
class DragonlordOjutaiTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof while untapped and loses it when tapped")
    void hasHexproofOnlyWhileUntapped() {
        Permanent ojutai = addOjutai(player2);

        assertThat(gqs.hasKeyword(gd, ojutai, Keyword.HEXPROOF)).isTrue();

        ojutai.tap();

        assertThat(gqs.hasKeyword(gd, ojutai, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Untapped Dragonlord Ojutai cannot be targeted")
    void untappedOjutaiCannotBeTargeted() {
        Permanent ojutai = addOjutai(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ojutai.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Tapped Dragonlord Ojutai can be targeted")
    void tappedOjutaiCanBeTargeted() {
        Permanent ojutai = addOjutai(player2);
        ojutai.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, ojutai.getId());
        harness.passBothPriorities();

        assertThat(ojutai.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage looks at the top three and puts one into hand")
    void combatDamageSelectsOneOfTopThree() {
        Permanent ojutai = addOjutai(player1);
        ojutai.setAttacking(true);

        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(first, second, third);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(
                        reorder.cards().indexOf(first), reorder.cards().indexOf(third))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addOjutai(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new DragonlordOjutai());
    }
}
