package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhantomGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Own creature tokens get +1/+1")
    void buffsOwnCreatureTokens() {
        harness.addToBattlefield(player1, new PhantomGeneral());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 1, 1));

        Permanent token = findPermanent(player1, "Soldier Token");

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff nontoken creatures you control")
    void doesNotBuffNontokenCreatures() {
        harness.addToBattlefield(player1, new PhantomGeneral());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nontoken Phantom General does not buff itself")
    void doesNotBuffItselfWhenNontoken() {
        harness.addToBattlefield(player1, new PhantomGeneral());

        Permanent general = findPermanent(player1, "Phantom General");

        assertThat(gqs.getEffectivePower(gd, general)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, general)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's creature tokens")
    void doesNotBuffOpponentTokens() {
        harness.addToBattlefield(player1, new PhantomGeneral());
        harness.addToBattlefield(player2, createTokenCreature("Zombie Token", 2, 2));

        Permanent opponentToken = findPermanent(player2, "Zombie Token");

        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Phantom Generals stack on a token")
    void twoGeneralsStack() {
        harness.addToBattlefield(player1, new PhantomGeneral());
        harness.addToBattlefield(player1, new PhantomGeneral());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 1, 1));

        Permanent token = findPermanent(player1, "Soldier Token");

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    private Card createTokenCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
