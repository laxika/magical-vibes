package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KelsienThePlague.class, GrizzlyBears.class, LlanowarElves.class})
class KelsienThePlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each experience counter its controller has")
    void getsPlusOnePlusOneForEachExperienceCounter() {
        gd.playerExperienceCounters.put(player1.getId(), 2);
        Permanent kelsien = harness.addToBattlefieldAndReturn(player1, new KelsienThePlague());
        assertThat(gqs.getEffectivePower(gd, kelsien)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kelsien)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets an experience counter when the damaged creature dies this turn")
    void getsExperienceCounterWhenDamagedCreatureDies() {
        harness.addToBattlefield(player1, new KelsienThePlague());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.passBothPriorities();
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Can register the experience trigger for a creature that dies later this turn")
    void getsExperienceCounterWhenTargetDiesLater() {
        harness.addToBattlefield(player1, new KelsienThePlague());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = bears.getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new KelsienThePlague());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Each resolved activation creates its own experience trigger")
    void repeatedActivationsEachGiveExperience() {
        Permanent kelsien = harness.addToBattlefieldAndReturn(player1, new KelsienThePlague());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());

        kelsien.untap();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, kelsien)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kelsien)).isEqualTo(4);
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("No experience is earned if the target dies before the ability resolves")
    void targetDyingBeforeResolutionDoesNotGiveExperience() {
        harness.addToBattlefield(player1, new KelsienThePlague());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, elves));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still deals damage and grants experience if Kelsien leaves before resolution")
    void abilityResolvesAfterKelsienLeaves() {
        Permanent kelsien = harness.addToBattlefieldAndReturn(player1, new KelsienThePlague());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kelsien));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kelsien, the Plague");
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature dying on a later turn does not grant experience")
    void delayedTriggerExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new KelsienThePlague());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only its controller's experience counters boost Kelsien")
    void ignoresOpponentsExperienceCounters() {
        gd.playerExperienceCounters.put(player1.getId(), 1);
        gd.playerExperienceCounters.put(player2.getId(), 5);
        Permanent kelsien = harness.addToBattlefieldAndReturn(player1, new KelsienThePlague());

        assertThat(gqs.getEffectivePower(gd, kelsien)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kelsien)).isEqualTo(3);
    }
}
