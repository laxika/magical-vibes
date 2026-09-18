package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GauntletOfMight.class, Mountain.class, Forest.class})
class GauntletOfMightTest extends BaseCardTest {

    @Test
    void redCreaturesGetBoostedOnBothBattlefields() {
        harness.addToBattlefield(player1, creature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player1, creature("Green Bear", 2, 2, CardColor.GREEN));
        harness.addToBattlefield(player2, creature("Red Goblin", 1, 1, CardColor.RED));
        addGauntlet();

        Permanent ownRed = findPermanent(player1, "Red Goblin");
        Permanent ownGreen = findPermanent(player1, "Green Bear");
        Permanent opponentRed = findPermanent(player2, "Red Goblin");

        assertThat(gqs.getEffectivePower(gd, ownRed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownRed)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownGreen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGreen)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentRed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentRed)).isEqualTo(2);
    }

    @Test
    void mountainAddsAdditionalRedManaForAnyPlayer() {
        addGauntlet();
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void nonMountainDoesNotAddAdditionalRedMana() {
        addGauntlet();
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private void addGauntlet() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GauntletOfMight()));
    }

    private static Card creature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
