package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NorthPoleGates.class})
class NorthPoleGatesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new NorthPoleGates()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "North Pole Gates").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds white mana")
    void tapsForWhiteMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NorthPoleGates());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds blue mana")
    void tapsForBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NorthPoleGates());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying four generic mana sacrifices the land and draws a card")
    void sacrificesAndDraws() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NorthPoleGates());
        NorthPoleGates draw = new NorthPoleGates();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Costs are paid before the draw ability resolves")
    void paysCostsBeforeDrawing() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NorthPoleGates());
        NorthPoleGates draw = new NorthPoleGates();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three mana cannot pay for the draw ability")
    void cannotDrawWithInsufficientMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NorthPoleGates());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land that entered tapped cannot activate either tap ability")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new NorthPoleGates()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "North Pole Gates");
        harness.assertNotInGraveyard(player1, "North Pole Gates");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
