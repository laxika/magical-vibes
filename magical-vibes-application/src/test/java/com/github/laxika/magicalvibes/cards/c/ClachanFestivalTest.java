package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClachanFestival.class})
class ClachanFestivalTest extends BaseCardTest {

    

    @Test
    @DisplayName("ETB creates two 1/1 green and white Kithkin creature tokens")
    void etbCreatesTwoKithkinTokens() {
        harness.setHand(player1, List.of(new ClachanFestival()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

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
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Kithkin");

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

    @Test
    @DisplayName("Entering creates tokens only when the triggered ability resolves")
    void enteringWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new ClachanFestival()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        assertThat(countKithkinTokens(player1)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Clachan Festival");
        assertThat(countKithkinTokens(player1)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countKithkinTokens(player1)).isEqualTo(2);
        assertThat(countKithkinTokens(player2)).isZero();
        assertThat(findPermanents(player1, "Kithkin")).allSatisfy(token -> {
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("Activated token creation waits for resolution and produces the full token characteristics")
    void activatedTokenHasOracleCharacteristics() {
        harness.addToBattlefield(player1, new ClachanFestival());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(countKithkinTokens(player1)).isZero();
        harness.passBothPriorities();

        assertThat(countKithkinTokens(player1)).isEqualTo(1);
        assertThat(countKithkinTokens(player2)).isZero();
        Permanent token = findPermanent(player1, "Kithkin");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KITHKIN);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Five colorless mana cannot pay the white component of the activation cost")
    void cannotActivateWithoutWhiteMana() {
        harness.addToBattlefield(player1, new ClachanFestival());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(countKithkinTokens(player1)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private int countKithkinTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .filter(p -> p.getCard().isToken())
                .count();
    }
}
