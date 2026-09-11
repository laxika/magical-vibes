package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZulaportCutthroat.class, GrizzlyBears.class, Shock.class})
class ZulaportCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("An ally creature dying makes each opponent lose 1 life and gains you 1 life")
    void allyCreatureDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, creature);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Zulaport Cutthroat's own death triggers its ability")
    void selfDeathDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent cutthroat = harness.addToBattlefieldAndReturn(player1, new ZulaportCutthroat());
        killWithShock(player1, cutthroat);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Zulaport Cutthroat")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, creature);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
