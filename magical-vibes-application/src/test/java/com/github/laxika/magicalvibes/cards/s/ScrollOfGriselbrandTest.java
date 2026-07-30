package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScrollOfGriselbrandTest extends BaseCardTest {

    private static Card createDemon() {
        Card card = new Card();
        card.setName("Abyssal Persecutor");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}{B}{B}");
        card.setColor(CardColor.BLACK);
        card.setPower(6);
        card.setToughness(6);
        card.setSubtypes(List.of(CardSubtype.DEMON));
        return card;
    }

    private void setupScroll() {
        harness.addToBattlefield(player1, new ScrollOfGriselbrand());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new GiantGrowth())));
    }

    @Test
    @DisplayName("Target opponent discards a card and the Scroll is sacrificed")
    void opponentDiscardsAndScrollSacrificed() {
        setupScroll();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent also loses 3 life when you control a Demon")
    void opponentLosesLifeWithDemon() {
        setupScroll();
        harness.addToBattlefield(player1, createDemon());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        setupScroll();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
