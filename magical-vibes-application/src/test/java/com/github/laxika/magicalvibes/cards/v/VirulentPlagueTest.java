package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VirulentPlague.class)
class VirulentPlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Creature tokens get -2/-2 regardless of controller")
    void debuffsCreatureTokens() {
        harness.addToBattlefield(player1, new VirulentPlague());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 3, 3));
        harness.addToBattlefield(player2, createTokenCreature("Zombie Token", 4, 4));

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
        harness.addToBattlefield(player1, new VirulentPlague());
        harness.addToBattlefield(player1, createCreature("Grizzly Bears", 2, 2));

        Permanent creature = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature tokens are unaffected")
    void doesNotDebuffNoncreatureTokens() {
        harness.addToBattlefield(player1, new VirulentPlague());
        harness.addToBattlefield(player1, createTokenArtifact("Treasure Token"));

        Permanent token = findPermanent(player1, "Treasure Token");

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature token with zero toughness is removed by state-based actions")
    void zeroToughnessCreatureTokenDies() {
        harness.addToBattlefield(player1, new VirulentPlague());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 2, 2));

        Permanent token = findPermanent(player1, "Soldier Token");
        assertThat(gqs.getEffectiveToughness(gd, token)).isZero();

        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Soldier Token")).isEmpty();
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
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

    private Card createTokenArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setToken(true);
        return card;
    }
}
