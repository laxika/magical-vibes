package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LifeGoesOnTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 4 life when no creature died this turn")
    void gains4LifeWithoutMorbid() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LifeGoesOn()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Gains 8 life when a creature died this turn")
    void gains8LifeWithMorbid() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LifeGoesOn()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("Morbid is checked at resolution time")
    void morbidCheckedAtResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LifeGoesOn()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);

        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("Killing a creature with Shock enables morbid")
    void actualCreatureDeathEnablesMorbid() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock(), new LifeGoesOn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }
}
