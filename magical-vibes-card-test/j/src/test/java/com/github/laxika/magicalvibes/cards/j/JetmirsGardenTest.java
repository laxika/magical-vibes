package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JetmirsGarden.class, JewelThief.class})
class JetmirsGardenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new JetmirsGarden()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        harness.addToBattlefieldAndReturn(player1, new JetmirsGarden()).setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        harness.addToBattlefieldAndReturn(player1, new JetmirsGarden()).setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        harness.addToBattlefieldAndReturn(player1, new JetmirsGarden()).setSummoningSick(false);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new JetmirsGarden()));
        harness.setLibrary(player1, List.of(new JewelThief()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Jetmir's Garden");
        harness.assertInHand(player1, "Jewel Thief");
    }

    @Test
    @DisplayName("Cycling pays three generic mana and discards before the draw resolves")
    void cyclingPaysCostBeforeDrawing() {
        harness.setHand(player1, List.of(new JetmirsGarden()));
        harness.setLibrary(player1, List.of(new JewelThief()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Jetmir's Garden");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Jewel Thief");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only two mana")
    void cyclingRequiresThreeMana() {
        harness.setHand(player1, List.of(new JetmirsGarden()));
        harness.setLibrary(player1, List.of(new JewelThief()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Jetmir's Garden");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Garden cannot activate any of its mana abilities")
    void tappedGardenCannotProduceMana() {
        harness.setHand(player1, List.of(new JetmirsGarden()));
        harness.playLand(player1, 0);

        for (int abilityIndex = 0; abilityIndex < 3; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untapped Garden can produce mana immediately without using the stack")
    void landManaAbilityIgnoresSummoningSickness() {
        var garden = harness.addToBattlefieldAndReturn(player1, new JetmirsGarden());
        garden.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(garden.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
