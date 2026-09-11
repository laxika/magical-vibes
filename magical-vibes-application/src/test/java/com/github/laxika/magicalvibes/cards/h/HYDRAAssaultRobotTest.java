package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.k.KingpinsEnforcers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HYDRAAssaultRobot.class, KingpinsEnforcers.class, CopperMyr.class})
class HYDRAAssaultRobotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage when another nonartifact Villain enters under your control")
    void triggersForNonartifactVillain() {
        addRobot();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KingpinsEnforcers()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveTriggeredDamage();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage when another artifact enters under your control")
    void triggersForArtifact() {
        addRobot();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveTriggeredDamage();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals only 1 damage when another artifact Villain enters")
    void artifactVillainTriggersOnlyOnce() {
        addRobot();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HYDRAAssaultRobot()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveTriggeredDamage();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Permanent addRobot() {
        return harness.addToBattlefieldAndReturn(player1, new HYDRAAssaultRobot());
    }

    private void resolveTriggeredDamage() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
