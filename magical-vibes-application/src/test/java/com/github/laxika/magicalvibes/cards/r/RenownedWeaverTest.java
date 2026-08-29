package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RenownedWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself to create a 1/3 green enchantment Spider with reach")
    void createsSpiderToken() {
        harness.addToBattlefield(player1, new RenownedWeaver());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Renowned Weaver");
        List<Permanent> spiders = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spider"))
                .toList();

        assertThat(spiders).singleElement().satisfies(spider -> {
            assertThat(spider.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(spider.getCard().getPower()).isEqualTo(1);
            assertThat(spider.getCard().getToughness()).isEqualTo(3);
            assertThat(spider.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(spider.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
            assertThat(spider.getCard().getSubtypes()).contains(CardSubtype.SPIDER);
            assertThat(spider.getCard().getKeywords()).contains(Keyword.REACH);
        });
    }

    @Test
    @DisplayName("Cannot activate without paying {1}{G}")
    void requiresMana() {
        harness.addToBattlefield(player1, new RenownedWeaver());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Renowned Weaver");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Spider"));
    }
}
