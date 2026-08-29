package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HallOfTriumphTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    void resolvingAwaitsColorChoice() {
        harness.setHand(player1, List.of(new HallOfTriumph()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hall of Triumph");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    void chosenColorBoostsOnlyMatchingCreaturesYouControl() {
        harness.addToBattlefield(player1, createCreature("Green Bear", 2, 2, CardColor.GREEN));
        harness.addToBattlefield(player1, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player2, createCreature("Green Bear", 2, 2, CardColor.GREEN));
        harness.setHand(player1, List.of(new HallOfTriumph()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent ownGreen = findPermanent(player1, "Green Bear");
        assertThat(gqs.getEffectivePower(gd, ownGreen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGreen)).isEqualTo(2);

        harness.handleListChoice(player1, "GREEN");

        Permanent ownRed = findPermanent(player1, "Red Goblin");
        Permanent opponentGreen = findPermanent(player2, "Green Bear");
        assertThat(gqs.getEffectivePower(gd, ownGreen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownGreen)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownRed)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownRed)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentGreen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGreen)).isEqualTo(2);
    }
}
