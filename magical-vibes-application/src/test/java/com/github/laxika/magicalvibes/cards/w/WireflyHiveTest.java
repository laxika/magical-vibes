package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WireflyHive.class)
class WireflyHiveTest extends BaseCardTest {

    @Test
    @DisplayName("Coin flip creates Wirefly or destroys all permanents named Wirefly")
    void coinFlipResolvesMatchingOutcome() {
        Permanent hive = harness.addToBattlefieldAndReturn(player1, new WireflyHive());
        harness.addToBattlefield(player2, new WireflyHive());
        harness.addToBattlefield(player1, wirefly());
        harness.addToBattlefield(player2, wirefly());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Wirefly Hive")).isTrue();
        assertThat(hive.isTapped()).isTrue();
        assertThat(countNamedPermanents("Wirefly Hive")).isEqualTo(2);

        boolean wonFlip = gameLogContains("wins the coin flip for Wirefly Hive");
        if (wonFlip) {
            Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(permanent -> permanent.getCard().isToken())
                    .filter(permanent -> permanent.getCard().getName().equals("Wirefly"))
                    .findFirst().orElseThrow();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(countNamedPermanents("Wirefly")).isEqualTo(3);
        } else {
            assertThat(gameLogContains("loses the coin flip for Wirefly Hive")).isTrue();
            assertThat(countNamedPermanents("Wirefly")).isZero();
        }
    }

    @Test
    @DisplayName("Activation pays three generic mana and flips only when the ability resolves")
    void activationPaysManaAndFlipsOnResolution() {
        Permanent hive = harness.addToBattlefieldAndReturn(player1, new WireflyHive());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(hive.isTapped()).isTrue();
        assertThat(gameLogContains("coin flip for Wirefly Hive")).isFalse();

        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Wirefly Hive")).isTrue();
    }

    private long countNamedPermanents(String name) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .count();
    }

    private static Card wirefly() {
        Card card = new Card();
        card.setName("Wirefly");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
