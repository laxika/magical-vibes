package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AgentOfAtlas;
import com.github.laxika.magicalvibes.cards.c.CaptainAmericasShield;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.k.KingpinsEnforcers;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HYDRAAssaultRobot.class, KingpinsEnforcers.class, CopperMyr.class, CaptainAmericasShield.class, AgentOfAtlas.class})
class HYDRAAssaultRobotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage when another nonartifact Villain enters under your control")
    void triggersForNonartifactVillain() {
        addRobot();
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new KingpinsEnforcers(), "{2}{B}");
        resolveTriggeredDamage();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage when another artifact enters under your control")
    void triggersForArtifact() {
        addRobot();
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new CopperMyr(), "{2}");
        resolveTriggeredDamage();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals only 1 damage when another artifact Villain enters")
    void artifactVillainTriggersOnlyOnce() {
        addRobot();
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new HYDRAAssaultRobot(), "{1}{R}");
        resolveTriggeredDamage();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotTriggerForItself() {
        harness.castFromHand(player1, new HYDRAAssaultRobot(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's Villain")
    void doesNotTriggerForOpponentsVillain() {
        addRobot();
        harness.enterBattlefieldAndReturn(player2, new KingpinsEnforcers());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's artifact Villain")
    void doesNotTriggerForOpponentsArtifactVillain() {
        addRobot();
        harness.enterBattlefieldAndReturn(player2, new HYDRAAssaultRobot());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Triggers for a noncreature artifact")
    void triggersForNoncreatureArtifact() {
        addRobot();
        harness.castFromHand(player1, new CaptainAmericasShield(), "{2}");
        resolveTriggeredDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers for each qualifying entry in the same turn")
    void triggersForEachEntry() {
        addRobot();
        harness.castFromHand(player1, new KingpinsEnforcers(), "{2}{B}");
        resolveTriggeredDamage();
        harness.castFromHand(player1, new CaptainAmericasShield(), "{2}");
        resolveTriggeredDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a nonartifact creature without Villain")
    void doesNotTriggerForNonvillainCreature() {
        addRobot();
        harness.castFromHand(player1, new AgentOfAtlas(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertLife(player2, 20);
    }

    private void addRobot() {
        harness.addToBattlefield(player1, new HYDRAAssaultRobot());
    }

    private void resolveTriggeredDamage() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
