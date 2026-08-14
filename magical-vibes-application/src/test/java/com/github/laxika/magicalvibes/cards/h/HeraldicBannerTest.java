package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeraldicBannerTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color boosts only matching creatures you control")
    void choosingColorBoostsMatchingOwnCreatures() {
        Card redCreature = createCreature("Raging Goblin", "{R}", 1, 1, CardColor.RED);
        Card greenCreature = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN);
        Card opponentRedCreature = createCreature("Raging Goblin", "{R}", 1, 1, CardColor.RED);
        Permanent redPermanent = harness.addToBattlefieldAndReturn(player1, redCreature);
        Permanent greenPermanent = harness.addToBattlefieldAndReturn(player1, greenCreature);
        Permanent opponentRedPermanent = harness.addToBattlefieldAndReturn(player2, opponentRedCreature);

        harness.setHand(player1, java.util.List.of(new HeraldicBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        Permanent banner = findPermanent(player1, "Heraldic Banner");
        assertThat(banner.getChosenColor()).isEqualTo(CardColor.RED);
        assertThat(gqs.getEffectivePower(gd, redPermanent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, redPermanent)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, greenPermanent)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentRedPermanent)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability adds one mana of the chosen color")
    void tapAbilityAddsChosenColorMana() {
        harness.addToBattlefield(player1, new HeraldicBanner());
        Permanent banner = findPermanent(player1, "Heraldic Banner");
        banner.setChosenColor(CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(banner.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private static Card createCreature(String name, String manaCost, int power, int toughness,
                                       CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
