package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClachanFestivalTest extends BaseCardTest {


    @Test
    @DisplayName("Clachan Festival has correct card properties")
    void hasCorrectProperties() {
        ClachanFestival card = new ClachanFestival();

        assertThat(card.getAdditionalTypes()).contains(CardType.KINDRED);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect etbEffect =
                (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etbEffect.amount()).isEqualTo(2);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{4}{W}");
        assertThat(ability.isNeedsTarget()).isFalse();
    }

    @Test
    @DisplayName("ETB creates two 1/1 green and white Kithkin creature tokens")
    void etbCreatesTwoKithkinTokens() {
        harness.setHand(player1, List.of(new ClachanFestival()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3); // enchantment + 2 tokens
        assertThat(countKithkinTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kithkin tokens are 1/1 with correct subtypes")
    void kithkinTokensHaveCorrectStats() {
        harness.setHand(player1, List.of(new ClachanFestival()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin"))
                .findFirst()
                .orElseThrow();

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KITHKIN);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Activated ability creates one 1/1 Kithkin creature token")
    void activatedAbilityCreatesOneKithkinToken() {
        harness.addToBattlefield(player1, new ClachanFestival());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countKithkinTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability can be used multiple times with enough mana")
    void activatedAbilityCanBeUsedMultipleTimes() {
        harness.addToBattlefield(player1, new ClachanFestival());
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countKithkinTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new ClachanFestival());
        harness.addMana(player1, ManaColor.WHITE, 4); // need 5 total ({4}{W})

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private int countKithkinTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .filter(p -> p.getCard().isToken())
                .count();
    }
}
