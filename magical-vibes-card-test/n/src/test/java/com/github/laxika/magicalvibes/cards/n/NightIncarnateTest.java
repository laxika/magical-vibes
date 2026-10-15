package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightIncarnate.class, AirElemental.class, DoomBlade.class})
class NightIncarnateTest extends BaseCardTest {

    @Test
    @DisplayName("When it leaves, all creatures get -3/-3 until end of turn")
    void leaveTriggerDebuffsAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent nightIncarnate = harness.addToBattlefieldAndReturn(player1, new NightIncarnate());

        destroyNightIncarnate(nightIncarnate);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The leave-triggered debuff expires at end of turn")
    void leaveTriggerDebuffExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent nightIncarnate = harness.addToBattlefieldAndReturn(player1, new NightIncarnate());

        destroyNightIncarnate(nightIncarnate);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Evoke sacrifices Night Incarnate and its leave trigger resolves")
    void evokeSacrificesSelfAndTriggersDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new NightIncarnate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Night Incarnate");
        harness.assertInGraveyard(player1, "Night Incarnate");
    }

    private void destroyNightIncarnate(Permanent nightIncarnate) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, nightIncarnate.getId());
        harness.passBothPriorities();
    }
}
