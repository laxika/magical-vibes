package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.f.FanaticalFirebrand;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldicBanner.class, FanaticalFirebrand.class, LlanowarElves.class,
        PaintersServant.class, EnsoulArtifact.class})
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

        harness.castFromHand(player1, new HeraldicBanner(), "{3}");
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

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Each color can be chosen and produces exactly one mana without using the stack")
    void chosenColorDeterminesManaImmediately(CardColor color) {
        harness.castFromHand(player1, new HeraldicBanner(), "{3}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, color.name());

        harness.activateAbility(player1, 0, 0, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(findPermanent(player1, "Heraldic Banner").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple Banners stack and boost matching creatures entering later even while tapped")
    void multipleBannersBoostLaterCreaturesWhileTapped() {
        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new HeraldicBanner(), "{3}");
            harness.passBothPriorities();
            harness.handleListChoice(player1, "RED");
            harness.activateAbility(player1, i, 0, null, null);
        }

        Permanent red = harness.enterBattlefieldAndReturn(player1, new FanaticalFirebrand());
        Permanent green = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentRed = harness.enterBattlefieldAndReturn(player2, new FanaticalFirebrand());

        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentRed)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Banner animated into a creature of its chosen color boosts itself")
    void animatedBannerOfChosenColorBoostsItself() {
        harness.castFromHand(player1, new HeraldicBanner(), "{3}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        Permanent banner = findPermanent(player1, "Heraldic Banner");

        harness.castFromHand(player1, new PaintersServant(), "{2}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, banner.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, banner)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, banner)).isEqualTo(5);
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
