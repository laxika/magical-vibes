package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Moat.class, DurkwoodBoars.class, ZephyrFalcon.class})
class MoatTest extends BaseCardTest {

    @Test
    @DisplayName("A creature without flying cannot attack while Moat is on the battlefield")
    void nonFlyingCreatureCannotAttack() {
        harness.addToBattlefield(player1, new Moat());
        addCreatureReady(player2, new DurkwoodBoars());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with flying can attack while Moat is on the battlefield")
    void flyingCreatureCanAttack() {
        harness.addToBattlefield(player1, new Moat());
        addCreatureReady(player2, new ZephyrFalcon());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("A non-flying creature controlled by Moat's controller cannot attack")
    void controllersNonFlyingCreatureCannotAttack() {
        harness.addToBattlefield(player1, new Moat());
        addCreatureReady(player1, new DurkwoodBoars());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures can attack again after Moat leaves the battlefield")
    void restrictionEndsWhenMoatLeaves() {
        Permanent moat = harness.addToBattlefieldAndReturn(player1, new Moat());
        addCreatureReady(player2, new DurkwoodBoars());
        gd.playerBattlefields.get(player1.getId()).remove(moat);
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 16);
    }
}
