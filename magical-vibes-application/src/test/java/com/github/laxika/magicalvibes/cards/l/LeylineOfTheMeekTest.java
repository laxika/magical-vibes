package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LeylineOfTheMeek.class, GrizzlyBears.class})
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
    @DisplayName("Non-token creatures are unaffected")
    void doesNotBoostNonTokenCreatures() {
        harness.addToBattlefield(player1, new LeylineOfTheMeek());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Leyline in the opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTheMeek()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of the Meek"));
    }

    @Test
    @DisplayName("Declining the opening-hand ability keeps Leyline of the Meek in hand")
    void decliningOpeningHandAbilityKeepsLeylineInHand() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTheMeek()));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leyline of the Meek"));
        assertThat(openingHarness.getGameData().playerHands
                .get(openingHarness.getPlayer1().getId()))
                .anyMatch(c -> c.getName().equals("Leyline of the Meek"));
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
