package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChameleonSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color sets power and toughness to matching opponent permanents")
    void countsMatchingOpponentPermanentsAfterChoosingColor() {
        harness.addToBattlefield(player2, coloredPermanent("Blue Permanent", CardColor.BLUE));
        harness.addToBattlefield(player2, coloredPermanent("Blue-Green Permanent", CardColor.BLUE, CardColor.GREEN));
        harness.addToBattlefield(player2, coloredPermanent("Green Permanent", CardColor.GREEN));
        harness.addToBattlefield(player1, coloredPermanent("Own Blue Permanent", CardColor.BLUE));

        harness.setHand(player1, List.of(new ChameleonSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        Permanent spirit = findSpirit(player1);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power and toughness update as matching opponent permanents change")
    void updatesWhenMatchingOpponentPermanentsChange() {
        Permanent spirit = new Permanent(new ChameleonSpirit());
        spirit.setChosenColor(CardColor.BLUE);
        gd.playerBattlefields.get(player1.getId()).add(spirit);

        harness.addToBattlefield(player2, coloredPermanent("Blue Permanent", CardColor.BLUE));
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);

        harness.addToBattlefield(player2, coloredPermanent("Another Blue Permanent", CardColor.BLUE));
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Blue Permanent"));
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("No chosen color means no matching permanents")
    void noChosenColorMeansZeroPowerAndToughness() {
        Permanent spirit = new Permanent(new ChameleonSpirit());
        gd.playerBattlefields.get(player1.getId()).add(spirit);
        harness.addToBattlefield(player2, coloredPermanent("Blue Permanent", CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, spirit)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isZero();
    }

    private Permanent findSpirit(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Chameleon Spirit"))
                .findFirst()
                .orElseThrow();
    }

    private static Card coloredPermanent(String name, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColors(List.of(colors));
        card.setColor(colors[0]);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
