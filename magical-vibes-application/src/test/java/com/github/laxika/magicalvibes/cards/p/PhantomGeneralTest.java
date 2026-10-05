package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DramaticRescue;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomGeneral.class, GrizzlyBears.class, DramaticRescue.class})
class PhantomGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Own creature tokens get +1/+1")
    void buffsOwnCreatureTokens() {
        harness.addToBattlefield(player1, new PhantomGeneral());
        Permanent token = harness.addToBattlefieldAndReturn(player1, createTokenCreature("Soldier Token", 1, 1));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff nontoken creatures you control")
    void doesNotBuffNontokenCreatures() {
        harness.addToBattlefield(player1, new PhantomGeneral());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nontoken Phantom General does not buff itself")
    void doesNotBuffItselfWhenNontoken() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new PhantomGeneral());

        assertThat(gqs.getEffectivePower(gd, general)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, general)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's creature tokens")
    void doesNotBuffOpponentTokens() {
        harness.addToBattlefield(player1, new PhantomGeneral());
        Permanent opponentToken = harness.addToBattlefieldAndReturn(player2, createTokenCreature("Zombie Token", 2, 2));

        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Phantom Generals stack on a token")
    void twoGeneralsStack() {
        harness.addToBattlefield(player1, new PhantomGeneral());
        harness.addToBattlefield(player1, new PhantomGeneral());
        Permanent token = harness.addToBattlefieldAndReturn(player1, createTokenCreature("Soldier Token", 1, 1));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("A token copy of Phantom General buffs itself and other creature tokens")
    void tokenGeneralBuffsItself() {
        PhantomGeneral tokenCopy = new PhantomGeneral();
        tokenCopy.setToken(true);
        Permanent general = harness.addToBattlefieldAndReturn(player1, tokenCopy);
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, createTokenCreature("Soldier Token", 1, 1));

        assertThat(gqs.getEffectivePower(gd, general)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, general)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature tokens lose the bonus when Phantom General leaves the battlefield")
    void bonusEndsWhenGeneralLeaves() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new PhantomGeneral());
        Permanent token = harness.addToBattlefieldAndReturn(player1, createTokenCreature("Soldier Token", 1, 1));
        harness.setHand(player1, List.of(new DramaticRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, general.getId());

        harness.assertInHand(player1, "Phantom General");
        harness.assertNotOnBattlefield(player1, "Phantom General");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncreature tokens do not get the bonus")
    void doesNotBuffNoncreatureTokens() {
        harness.addToBattlefield(player1, new PhantomGeneral());
        Card tokenCard = new Card();
        tokenCard.setName("Artifact Token");
        tokenCard.setType(CardType.ARTIFACT);
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        assertThat(gqs.getEffectivePower(gd, token)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, token)).isZero();
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
