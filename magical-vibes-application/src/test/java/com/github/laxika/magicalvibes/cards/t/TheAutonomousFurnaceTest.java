package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
