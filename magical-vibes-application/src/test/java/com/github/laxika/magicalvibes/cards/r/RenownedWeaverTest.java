package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RenownedWeaver.class})
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

    @Test
    @DisplayName("Sacrifice is paid immediately, but the Spider is created only on resolution")
    void sacrificesBeforeResolution() {
        harness.addToBattlefield(player1, new RenownedWeaver());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Renowned Weaver");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Weaver can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new RenownedWeaver());
        weaver.tap();
        weaver.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Renowned Weaver");
        harness.assertOnBattlefield(player1, "Spider");
    }

    @Test
    @DisplayName("Generic mana cannot pay the green part of the activation cost")
    void requiresGreenMana() {
        harness.addToBattlefield(player1, new RenownedWeaver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Renowned Weaver");
        harness.assertNotInGraveyard(player1, "Renowned Weaver");
        assertThat(gd.stack).isEmpty();
    }
}
