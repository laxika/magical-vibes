package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TreasureDredger.class)
class TreasureDredgerTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T}, Pay 1 life creates a Treasure token")
    void createsTreasureToken() {
        Permanent dredger = harness.addToBattlefieldAndReturn(player1, new TreasureDredger());
        dredger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(dredger.isTapped()).isTrue();
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Treasure Dredger cannot activate without {1}")
    void cannotActivateWithoutMana() {
        Permanent dredger = harness.addToBattlefieldAndReturn(player1, new TreasureDredger());
        dredger.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysCostsImmediatelyButCreatesTreasureOnlyOnResolution() {
        Permanent dredger = harness.addToBattlefieldAndReturn(player1, new TreasureDredger());
        dredger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(dredger.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(dredger);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent dredger = harness.addToBattlefieldAndReturn(player1, new TreasureDredger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(dredger.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent dredger = harness.addToBattlefieldAndReturn(player1, new TreasureDredger());
        dredger.setSummoningSick(false);
        dredger.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureCanImmediatelyBeSacrificedForAnyColor(ManaColor chosenColor) {
        Permanent dredger = harness.addToBattlefieldAndReturn(player1, new TreasureDredger());
        dredger.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, chosenColor.name());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(dredger);
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == chosenColor ? 1 : 0);
        }
    }
}
