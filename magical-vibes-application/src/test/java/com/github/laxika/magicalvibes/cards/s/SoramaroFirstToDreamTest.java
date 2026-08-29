package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoramaroFirstToDreamTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of cards in its controller's hand")
    void powerAndToughnessEqualHandSize() {
        SoramaroFirstToDream soramaro = new SoramaroFirstToDream();
        Permanent permanent = new Permanent(soramaro);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);

        harness.setHand(player1, List.of(card(), card()));
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);

        harness.setHand(player1, List.of(card(), card(), card(), card()));
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(4);
    }

    @Test
    @DisplayName("Returns a land as a cost and draws a card")
    void returnsLandAndDrawsCard() {
        Permanent soramaro = addSoramaro();
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(card()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(soramaro), 0, null);

        harness.assertInHand(player1, "Island");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        Permanent soramaro = addSoramaro();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(soramaro), 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSoramaro() {
        Permanent permanent = new Permanent(new SoramaroFirstToDream());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Card card() {
        Card card = new Card();
        card.setName("Filler Card");
        card.setType(CardType.INSTANT);
        return card;
    }
}
