package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuffUnderdogChamp.class, GrizzlyBears.class})
class RuffUnderdogChampTest extends BaseCardTest {

    private static Card creature(String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    @Test
    @DisplayName("Makes Hounds Dogs and buffs own Dogs after a match loss")
    void appliesDogTypeAndUnderdogBonus() {
        harness.addToBattlefield(player1, new RuffUnderdogChamp());
        Permanent hound = harness.addToBattlefieldAndReturn(player1, creature("Hound", 1, 1, CardSubtype.HOUND));
        Permanent dog = harness.addToBattlefieldAndReturn(player1, creature("Dog", 2, 2, CardSubtype.DOG));
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingHound = harness.addToBattlefieldAndReturn(player2,
                creature("Opposing Hound", 1, 1, CardSubtype.HOUND));

        assertThat(gqs.effectiveCreatureSubtypes(gd, hound))
                .contains(CardSubtype.HOUND, CardSubtype.DOG);
        assertThat(gqs.getEffectivePower(gd, hound)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingHound)).isEqualTo(1);

        gd.playersWhoLostGameThisMatch.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, hound)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hound)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingHound)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not apply Underdog before the controller loses a match game")
    void doesNotApplyUnderdogWithoutMatchLoss() {
        harness.addToBattlefield(player1, new RuffUnderdogChamp());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, creature("Hound", 1, 1, CardSubtype.HOUND));

        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(1);
    }
}
