package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IllnessInTheRanksTest extends BaseCardTest {

    @Test
    @DisplayName("Creature tokens get -1/-1 regardless of controller")
    void debuffsCreatureTokens() {
        harness.addToBattlefield(player1, new IllnessInTheRanks());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 2, 2));
        harness.addToBattlefield(player2, createTokenCreature("Zombie Token", 3, 3));

        Permanent ownToken = findPermanent(player1, "Soldier Token");
        Permanent opponentToken = findPermanent(player2, "Zombie Token");

        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nontoken creatures are unaffected")
    void doesNotDebuffNontokenCreatures() {
        harness.addToBattlefield(player1, new IllnessInTheRanks());
        harness.addToBattlefield(player1, createCreature("Grizzly Bears", 2, 2));

        Permanent creature = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A 1/1 creature token dies after the static debuff is applied")
    void oneOneTokenDiesToDebuff() {
        harness.addToBattlefield(player1, new IllnessInTheRanks());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 1, 1));

        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Soldier Token")).isEmpty();
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Card createTokenCreature(String name, int power, int toughness) {
        Card card = createCreature(name, power, toughness);
        card.setToken(true);
        return card;
    }
}
