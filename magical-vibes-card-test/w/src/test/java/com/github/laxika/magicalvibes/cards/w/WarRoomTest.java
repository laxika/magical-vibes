package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelOfJubilation;
import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarRoom.class, EdgarMarkov.class, AngelOfJubilation.class, PlatinumEmperion.class})
class WarRoomTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new WarRoom());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays one life for each color in the commander's color identity and draws")
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
    @DisplayName("Can't activate the draw ability without enough life")
    void cannotActivateWithoutEnoughLife() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        harness.addToBattlefield(player1, new WarRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Pays its costs as it is activated, before the draw resolves")
    void paysCostsBeforeDrawing() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        harness.setLibrary(player1, List.of(new WarRoom()));
        harness.addToBattlefield(player1, new WarRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertLife(player1, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Can't activate the draw ability without a commander")
    void cannotActivateWithoutACommander() {
        gd.playerCommanders.put(player1.getId(), List.of());
        gd.playerCommanders.put(player2.getId(), List.of(new EdgarMarkov()));
        harness.setLibrary(player1, List.of(new WarRoom()));
        harness.addToBattlefield(player1, new WarRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A commander with no colors in its color identity makes the draw cost no life")
    void colorlessCommanderPaysNoLife() {
        gd.playerCommanders.put(player1.getId(), List.of(new WarRoom()));
        harness.setLibrary(player1, List.of(new WarRoom()));
        harness.addToBattlefield(player1, new WarRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("Can't pay the draw cost while its controller's life total can't change")
    void cannotPayWhileLifeTotalCantChange() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        harness.addToBattlefield(player1, new WarRoom());
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't pay life");

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can't pay the life cost while Angel of Jubilation forbids paying life")
    void cannotPayCommanderLifeCostWhileAngelOfJubilationIsOnBattlefield() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        harness.addToBattlefield(player1, new WarRoom());
        harness.addToBattlefield(player2, new AngelOfJubilation());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
