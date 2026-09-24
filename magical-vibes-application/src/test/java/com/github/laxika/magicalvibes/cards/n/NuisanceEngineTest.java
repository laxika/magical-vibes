package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(NuisanceEngine.class)
class NuisanceEngineTest extends BaseCardTest {

    @Test
    @DisplayName("{2}, {T} creates a 0/1 colorless Pest artifact creature token")
    void createsPestToken() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new NuisanceEngine());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(engine.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        Permanent pest = findPermanent(player1, "Pest");
        assertThat(pest.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pest)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(1);
        assertThat(gqs.isArtifact(gd, pest)).isTrue();
        assertThat(gqs.isCreature(gd, pest)).isTrue();
        assertThat(pest.getCard().getSubtypes()).contains(CardSubtype.PEST);
        assertThat(gqs.getEffectiveColors(gd, pest)).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the ability without two mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new NuisanceEngine());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(engine.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Pest")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the ability while Nuisance Engine is tapped")
    void cannotActivateWhileTapped() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new NuisanceEngine());
        engine.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(countPermanents(player1, "Pest")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
