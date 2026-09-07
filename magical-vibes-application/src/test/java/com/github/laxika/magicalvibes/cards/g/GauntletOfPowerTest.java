package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GauntletOfPower.class, Forest.class, Mountain.class, ZhalfirinVoid.class})
class GauntletOfPowerTest extends BaseCardTest {

    @Test
    void resolvingAwaitsColorChoice() {
        harness.setHand(player1, List.of(new GauntletOfPower()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    void chosenColorBoostsOnlyMatchingCreaturesYouControl() {
        harness.addToBattlefield(player1, creature("Green Bear", 2, 2, CardColor.GREEN));
        harness.addToBattlefield(player1, creature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player2, creature("Green Bear", 2, 2, CardColor.GREEN));
        addChosenGauntlet(CardColor.GREEN);

        Permanent ownGreen = findPermanent(player1, "Green Bear");
        Permanent ownRed = findPermanent(player1, "Red Goblin");
        Permanent opponentGreen = findPermanent(player2, "Green Bear");

        assertThat(gqs.getEffectivePower(gd, ownGreen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownGreen)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownRed)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownRed)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentGreen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGreen)).isEqualTo(2);
    }

    @Test
    void matchingBasicLandAddsManaForAnyPlayer() {
        addChosenGauntlet(CardColor.GREEN);
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void nonbasicLandDoesNotAddExtraMana() {
        addChosenGauntlet(CardColor.GREEN);
        harness.addToBattlefield(player1, new ZhalfirinVoid());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void basicLandOfAnotherColorDoesNotAddExtraMana() {
        addChosenGauntlet(CardColor.GREEN);
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void addChosenGauntlet(CardColor color) {
        Permanent gauntlet = new Permanent(new GauntletOfPower());
        gauntlet.setChosenColor(color);
        gd.playerBattlefields.get(player1.getId()).add(gauntlet);
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
