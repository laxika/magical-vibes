package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WarRoom.class)
class WarRoomTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new WarRoom());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability pays life for each commander color and draws a card")
    void paysLifeForCommanderColorsAndDraws() {
        setCommanderColorIdentity(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK);
        harness.addToBattlefield(player1, new WarRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The second ability cannot be activated without enough life for the commander colors")
    void rejectsActivationWithoutEnoughLife() {
        setCommanderColorIdentity(CardColor.WHITE, CardColor.BLUE);
        harness.addToBattlefield(player1, new WarRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    private void setCommanderColorIdentity(CardColor... colors) {
        Card commander = new Card();
        commander.setColorIdentity(List.of(colors));
        gd.playerCommanders.put(player1.getId(), List.of(commander));
    }
}
