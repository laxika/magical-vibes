package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DreamstoneHedron.class)
class DreamstoneHedronTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dreamstone Hedron adds three colorless mana")
    void tapsForThreeColorlessMana() {
        Permanent hedron = harness.addToBattlefieldAndReturn(player1, new DreamstoneHedron());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(hedron.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying three mana and sacrificing Dreamstone Hedron draws three cards")
    void sacrificesAndDrawsThreeCards() {
        harness.addToBattlefield(player1, new DreamstoneHedron());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Dreamstone Hedron");
        harness.assertInGraveyard(player1, "Dreamstone Hedron");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("The draw ability cannot be activated without three mana")
    void cannotActivateDrawAbilityWithoutEnoughMana() {
        harness.addToBattlefield(player1, new DreamstoneHedron());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dreamstone Hedron");
    }
}
