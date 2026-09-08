package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotForgottenTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a targeted opponent graveyard card on top when chosen and creates a Spirit")
    void putsOpponentCardOnTopAndCreatesSpirit() {
        Card target = new GrizzlyBears();
        Card oldTop = new HolyDay();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(oldTop));
        castNotForgotten(target);

        harness.handleListChoice(player1, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target, oldTop);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertSpiritCreated();
    }

    @Test
    @DisplayName("Puts a targeted card on the bottom when chosen")
    void putsCardOnBottom() {
        Card target = new HolyDay();
        Card oldTop = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(oldTop));
        castNotForgotten(target);

        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(oldTop, target);
        assertSpiritCreated();
    }

    @Test
    @DisplayName("Fizzles without creating a Spirit if the targeted card leaves the graveyard")
    void fizzlesIfTargetLeavesGraveyard() {
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        castNotForgotten(target);
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    private void castNotForgotten(Card target) {
        harness.setHand(player1, List.of(new NotForgotten()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();
    }

    private void assertSpiritCreated() {
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
    }
}
