package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromiseOfPowerTest extends BaseCardTest {

    @Test
    @DisplayName("The draw mode draws five cards and loses 5 life")
    void drawMode() {
        cast(new int[]{0}, false, List.of(new PromiseOfPower()),
                List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("The token mode creates a Demon sized to the cards in hand")
    void tokenMode() {
        cast(new int[]{1}, false, List.of(
                        new PromiseOfPower(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()),
                List.of());

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getEffectivePower()).isEqualTo(4);
        assertThat(demon.getEffectiveToughness()).isEqualTo(4);
        assertThat(demon.getCard().getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Entwine resolves both modes and pays the additional four mana")
    void entwined() {
        cast(new int[]{0, 1}, true,
                List.of(new PromiseOfPower(), new GrizzlyBears(), new GrizzlyBears()),
                List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(demon.getEffectivePower()).isEqualTo(7);
        assertThat(demon.getEffectiveToughness()).isEqualTo(7);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine without the additional mana is rejected")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new PromiseOfPower()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, boolean entwined, List<Card> hand, List<Card> library) {
        harness.setHand(player1, hand);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 6 : 2);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }
}
