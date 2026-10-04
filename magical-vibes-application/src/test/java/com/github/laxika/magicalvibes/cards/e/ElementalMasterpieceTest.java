package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElementalMasterpiece.class})
class ElementalMasterpieceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 4/4 blue and red Elemental tokens")
    void createsElementalTokens() {
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Elemental");
            assertThat(token.getEffectivePower()).isEqualTo(4);
            assertThat(token.getEffectiveToughness()).isEqualTo(4);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        });
    }

    @Test
    @DisplayName("The hand ability pays two hybrid mana, discards the source, and creates a Treasure")
    void handAbilityCreatesTreasure() {
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
        harness.assertInGraveyard(player1, "Elemental Masterpiece");
    }

    @ParameterizedTest
    @CsvSource({"2, 0", "1, 1", "0, 2"})
    void hybridAbilityPaysAndDiscardsBeforeTreasureResolves(int blue, int red) {
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.BLUE, blue);
        harness.addMana(player1, ManaColor.RED, red);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Elemental Masterpiece");
        harness.assertInGraveyard(player1, "Elemental Masterpiece");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1)
                .allSatisfy(token -> assertThat(token.isTapped()).isFalse());
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"COLORLESS", "BLUE", "GREEN"})
    void insufficientOrWrongManaDoesNotDiscardSource(ManaColor color) {
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, color, color == ManaColor.BLUE ? 1 : 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Elemental Masterpiece");
        harness.assertNotInGraveyard(player1, "Elemental Masterpiece");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureSacrificesForOneManaOfAnyColor(ManaColor color) {
        harness.setHand(player1, List.of(new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
