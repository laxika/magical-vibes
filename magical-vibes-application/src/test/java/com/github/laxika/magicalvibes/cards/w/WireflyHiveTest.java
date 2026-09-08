package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WireflyHiveTest extends BaseCardTest {

    @Test
    @DisplayName("Coin flip creates Wirefly or destroys all permanents named Wirefly")
    void coinFlipResolvesMatchingOutcome() {
        addReady(player1, new WireflyHive());
        harness.addToBattlefield(player1, wirefly());
        harness.addToBattlefield(player2, wirefly());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        assertThat(logs).anyMatch(log -> log.contains("coin flip for Wirefly Hive"));

        boolean wonFlip = logs.stream().anyMatch(log -> log.contains("wins the coin flip for Wirefly Hive"));
        if (wonFlip) {
            Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(permanent -> permanent.getCard().isToken())
                    .filter(permanent -> permanent.getCard().getName().equals("Wirefly"))
                    .findFirst().orElseThrow();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(countNamedPermanents("Wirefly")).isEqualTo(3);
        } else {
            assertThat(logs).anyMatch(log -> log.contains("loses the coin flip for Wirefly Hive"));
            assertThat(countNamedPermanents("Wirefly")).isZero();
        }
    }

    private long countNamedPermanents(String name) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .count();
    }

    private void addReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
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
