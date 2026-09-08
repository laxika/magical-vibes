package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BottleOfSuleiman.class)
class BottleOfSuleimanTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sacrifices the artifact and produces exactly one flip outcome")
    void activatingFlipsCoin() {
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Sacrifice is a cost, so the Bottle is always gone from the battlefield.
        harness.assertNotOnBattlefield(player1, "Bottle of Suleiman");
        harness.assertInGraveyard(player1, "Bottle of Suleiman");

        boolean hasDjinn = countPermanents(player1, "Djinn") > 0;
        boolean took5 = gd.playerLifeTotals.get(player1.getId()) == lifeBefore - 5;

        // Exactly one branch resolves.
        assertThat(hasDjinn != took5)
                .as("Either create a 5/5 Djinn (win) or take 5 damage (loss)")
                .isTrue();

        if (hasDjinn) {
            assertThat(gameLogContains("wins the coin flip for Bottle of Suleiman")).isTrue();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        } else {
            assertThat(gameLogContains("loses the coin flip for Bottle of Suleiman")).isTrue();
        }
    }

    @Test
    @CardUsed(EdgarKingOfFigaro.class)
    @DisplayName("Winning the flip creates the specified Djinn token")
    void winningFlipCreatesCorrectDjinnToken() {
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("wins the coin flip for Bottle of Suleiman")).isTrue();

        Permanent djinn = findPermanent(player1, "Djinn");
        assertThat(djinn.getCard().isToken()).isTrue();
        assertThat(djinn.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(djinn.getCard().getAdditionalTypes()).containsExactly(CardType.ARTIFACT);
        assertThat(djinn.getCard().getColors()).isEmpty();
        assertThat(djinn.getCard().getSubtypes()).containsExactly(CardSubtype.DJINN);
        assertThat(djinn.getCard().getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(djinn.getCard().getPower()).isEqualTo(5);
        assertThat(djinn.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Coin flip happens when the activated ability resolves")
    void coinFlipHappensOnResolution() {
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gameLogContains("coin flip for Bottle of Suleiman")).isFalse();

        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Bottle of Suleiman")).isTrue();
    }

    @Test
    @DisplayName("Coin flip is logged for Bottle of Suleiman")
    void coinFlipLogged() {
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Bottle of Suleiman")).isTrue();
    }
}
