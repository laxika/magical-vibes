package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfTheMeek.class, LionheartMaverick.class})
class LeylineOfTheMeekTest extends BaseCardTest {

    @Test
    @DisplayName("Creature tokens get +1/+1 regardless of controller")
    void boostsCreatureTokens() {
        harness.addToBattlefield(player1, new LeylineOfTheMeek());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 1, 1));
        harness.addToBattlefield(player2, createTokenCreature("Zombie Token", 2, 2));

        Permanent ownToken = findPermanent(player1, "Soldier Token");
        Permanent opponentToken = findPermanent(player2, "Zombie Token");

        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(3);
    }

    @Test
    @DisplayName("Noncreature tokens are unaffected")
    void doesNotBoostNonCreatureTokens() {
        harness.addToBattlefield(player1, new LeylineOfTheMeek());
        harness.addToBattlefield(player1, createTokenCard("Relic Token", CardType.ARTIFACT, 3, 3));

        Permanent relic = findPermanent(player1, "Relic Token");

        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-token creatures are unaffected")
    void doesNotBoostNonTokenCreatures() {
        harness.addToBattlefield(player1, new LeylineOfTheMeek());
        harness.addToBattlefield(player1, new LionheartMaverick());

        Permanent maverick = findPermanent(player1, "Lionheart Maverick");

        assertThat(gqs.getEffectivePower(gd, maverick)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, maverick)).isEqualTo(1);
    }

    @Test
    @DisplayName("Leyline in the opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTheMeek()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        openingHarness.assertOnBattlefield(openingHarness.getPlayer1(), "Leyline of the Meek");
        openingHarness.assertNotInHand(openingHarness.getPlayer1(), "Leyline of the Meek");
    }

    @Test
    @DisplayName("Declining the opening-hand ability keeps Leyline of the Meek in hand")
    void decliningOpeningHandAbilityKeepsLeylineInHand() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTheMeek()));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        openingHarness.assertNotOnBattlefield(openingHarness.getPlayer1(), "Leyline of the Meek");
        openingHarness.assertInHand(openingHarness.getPlayer1(), "Leyline of the Meek");
    }

    private Card createTokenCreature(String name, int power, int toughness) {
        return createTokenCard(name, CardType.CREATURE, power, toughness);
    }

    private Card createTokenCard(String name, CardType type, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
