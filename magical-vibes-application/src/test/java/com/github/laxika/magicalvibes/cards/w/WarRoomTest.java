package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarRoom.class, EdgarMarkov.class})
class WarRoomTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new WarRoom());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void paysForCommanderColorIdentityAndDraws() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        harness.setLibrary(player1, List.of(new WarRoom()));
        harness.addToBattlefield(player1, new WarRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void cannotActivateWithoutEnoughLife() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        harness.addToBattlefield(player1, new WarRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }
}
