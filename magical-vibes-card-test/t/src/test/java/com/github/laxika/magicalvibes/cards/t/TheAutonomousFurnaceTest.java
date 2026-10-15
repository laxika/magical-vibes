package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheAutonomousFurnace.class, GrizzlyBears.class})
class TheAutonomousFurnaceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TheAutonomousFurnace()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one red mana")
    void tappingAddsRedMana() {
        Permanent furnace = harness.addToBattlefieldAndReturn(player1, new TheAutonomousFurnace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(furnace.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying one generic and one red mana sacrifices the land and draws a card")
    void sacrificesAndDraws() {
        Permanent furnace = harness.addToBattlefieldAndReturn(player1, new TheAutonomousFurnace());
        GrizzlyBears draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(furnace);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(furnace.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("A tapped Furnace cannot activate either ability")
    void tappedFurnaceCannotActivate() {
        harness.setHand(player1, List.of(new TheAutonomousFurnace()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice and mana are paid immediately but drawing waits for resolution")
    void sacrificeIsCostAndDrawUsesStack() {
        Permanent furnace = harness.addToBattlefieldAndReturn(player1, new TheAutonomousFurnace());
        TheAutonomousFurnace draw = new TheAutonomousFurnace();
        TheAutonomousFurnace remaining = new TheAutonomousFurnace();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw, remaining));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(furnace);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(furnace.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana alone cannot pay the draw ability's red cost")
    void drawAbilityRequiresRedMana() {
        Permanent furnace = harness.addToBattlefieldAndReturn(player1, new TheAutonomousFurnace());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(furnace);
        assertThat(furnace.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
