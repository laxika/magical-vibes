package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterfrontDistrict.class})
class WaterfrontDistrictTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new WaterfrontDistrict()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one blue mana")
    void tappingAddsBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WaterfrontDistrict());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds one black mana")
    void tappingAddsBlackMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WaterfrontDistrict());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying two generic, one blue, and one black mana sacrifices the land and draws a card")
    void sacrificesAndDraws() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WaterfrontDistrict());
        WaterfrontDistrict draw = new WaterfrontDistrict();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Entering without being played still enters tapped")
    void entersTappedWithoutBeingPlayed() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new WaterfrontDistrict());

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice and mana are paid before the draw resolves")
    void paysCostsBeforeDrawing() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WaterfrontDistrict());
        WaterfrontDistrict draw = new WaterfrontDistrict();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("A tapped land cannot activate either mana ability or the draw ability")
    void tappedLandCannotActivate() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new WaterfrontDistrict());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        for (int abilityIndex = 0; abilityIndex < 3; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("tapped");
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic mana cannot replace the required black mana")
    void drawAbilityRequiresBlackMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WaterfrontDistrict());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
