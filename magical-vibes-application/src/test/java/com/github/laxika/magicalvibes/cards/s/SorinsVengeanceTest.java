package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorinsVengeanceTest extends BaseCardTest {

    // "Sorin's Vengeance deals 10 damage to target player or planeswalker and you gain 10 life."

    private void giveSorinsVengeance() {
        harness.setHand(player1, List.of(new SorinsVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Deals 10 damage to the targeted player and the caster gains 10 life")
    void damageToPlayerAndLifeGain() {
        giveSorinsVengeance();
        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 10);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 10);
    }

    @Test
    @DisplayName("Targeting a planeswalker removes 10 loyalty and the caster still gains 10 life")
    void damageToPlaneswalker() {
        Permanent elspeth = new Permanent(new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 12);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);

        giveSorinsVengeance();
        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, elspeth.getId());
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 10);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        giveSorinsVengeance();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }
}
