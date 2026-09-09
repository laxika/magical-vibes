package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarTheGate.class, GrizzlyBears.class, JaceBeleren.class, Opt.class})
class BarTheGateTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and ventures into the dungeon")
    void countersCreatureSpellAndVenturesIntoDungeon() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new BarTheGate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Counters a planeswalker spell")
    void countersPlaneswalkerSpell() {
        JaceBeleren jace = new JaceBeleren();
        harness.setHand(player1, List.of(jace));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new BarTheGate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, jace.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jace Beleren");
        harness.assertNotOnBattlefield(player1, "Jace Beleren");
        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker spell")
    void cannotTargetNoncreatureNonplaneswalkerSpell() {
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new BarTheGate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, opt.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker spell");
    }
}
