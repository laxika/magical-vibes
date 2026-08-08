package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AkkiRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when an opponent's land is destroyed")
    void boostsWhenOpponentLandDestroyed() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        harness.addToBattlefield(player2, new Mountain());

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, mountainId);
        harness.passBothPriorities(); // Resolve Stone Rain — Mountain dies

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Akki Raider");

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers for the controller's own land too")
    void boostsWhenOwnLandDestroyed() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        harness.addToBattlefield(player1, new Mountain());

        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, mountainId);
        harness.passBothPriorities(); // Resolve Stone Rain
        harness.passBothPriorities(); // Resolve trigger

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when a non-land permanent dies")
    void doesNotTriggerOnNonLand() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict — Grizzly Bears dies

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +1/+0 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        harness.addToBattlefield(player2, new Mountain());

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, mountainId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
    }
}
