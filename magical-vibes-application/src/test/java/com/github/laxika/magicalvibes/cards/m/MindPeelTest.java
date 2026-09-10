package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindPeel.class, ManaLeak.class, SkyshroudFalcon.class})
class MindPeelTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card")
    void targetPlayerDiscards() {
        harness.setHand(player1, List.of(new MindPeel()));
        harness.setHand(player2, List.of(new SkyshroudFalcon()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Skyshroud Falcon");
        harness.assertInGraveyard(player1, "Mind Peel");
    }

    @Test
    @DisplayName("Paying buyback returns Mind Peel to its owner's hand")
    void buybackReturnsToHand() {
        harness.setHand(player1, List.of(new MindPeel()));
        harness.setHand(player2, List.of(new SkyshroudFalcon()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithBuyback(player1, 0, player2.getId());
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Mind Peel");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Skyshroud Falcon");
    }

    @Test
    @DisplayName("The caster may be the targeted player")
    void casterMayBeTargeted() {
        harness.setHand(player1, List.of(new MindPeel(), new SkyshroudFalcon()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Skyshroud Falcon");
        harness.assertInGraveyard(player1, "Mind Peel");
    }

    @Test
    @DisplayName("Targeting a player with no cards still resolves Mind Peel")
    void emptyTargetHandStillResolves() {
        harness.setHand(player1, List.of(new MindPeel()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mind Peel");
    }

    @Test
    @DisplayName("A countered buyback Mind Peel goes to the graveyard")
    void counteredBuybackGoesToGraveyard() {
        MindPeel spell = new MindPeel();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithBuyback(player1, 0, player2.getId());
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mind Peel");
        harness.assertInGraveyard(player2, "Mana Leak");
    }

    @Test
    @DisplayName("Mind Peel cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new SkyshroudFalcon());
        harness.setHand(player1, List.of(new MindPeel()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Skyshroud Falcon")))
                .isInstanceOf(IllegalStateException.class);
    }
}
