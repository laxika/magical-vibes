package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CallToGloryTest extends BaseCardTest {

    @Test
    void untapsOwnCreaturesAndBoostsSamurai() {
        Permanent samurai = addCreatureReady(player1, creature("Samurai", 2, 2, CardSubtype.SAMURAI));
        Permanent nonSamurai = addCreatureReady(player1, creature("Soldier", 2, 2, CardSubtype.SOLDIER));
        Permanent opponent = addCreatureReady(player2, creature("Opponent", 2, 2, CardSubtype.SOLDIER));
        samurai.tap();
        nonSamurai.tap();
        opponent.tap();

        harness.setHand(player1, List.of(new CallToGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(samurai.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);
        assertThat(nonSamurai.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, nonSamurai)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSamurai)).isEqualTo(2);
        assertThat(opponent.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    private static Card creature(String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
