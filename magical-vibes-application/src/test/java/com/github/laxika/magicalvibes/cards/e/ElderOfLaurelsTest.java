package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderOfLaurels.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class ElderOfLaurelsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts target creature by number of creatures controller controls")
    void boostsTargetByCreatureCount() {
        addCreatureReady(player1, new ElderOfLaurels());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        // Player1 controls 3 creatures: Elder, Grizzly Bears, Llanowar Elves

        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Target should get +3/+3 (3 creatures controlled by player1)
        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost counts only creatures, not other permanents")
    void boostCountsOnlyCreatures() {
        addCreatureReady(player1, new ElderOfLaurels());
        harness.addToBattlefield(player1, new Forest());
        // Player1 controls only the Elder (1 creature)

        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Target should get +1/+1 (only 1 creature: Elder itself)
        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new ElderOfLaurels());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // Player1 controls 2 creatures: Elder and Grizzly Bears

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Target should get +2/+2 (2 creatures controlled by player1)
        Permanent target = findPermanent(player1, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new ElderOfLaurels());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Counts creatures at resolution and fixes the boost afterward")
    void countsAtResolutionAndDoesNotRecalculate() {
        harness.addToBattlefield(player1, new ElderOfLaurels());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElderOfLaurels());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.addToBattlefield(player1, new ElderOfLaurels());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        harness.addToBattlefield(player1, new ElderOfLaurels());
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves with zero boost after the controller's last creature leaves")
    void resolvesAfterSourceLeavesWithNoCreatures() {
        harness.addToBattlefield(player1, new ElderOfLaurels());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElderOfLaurels());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A tapped summoning-sick Elder can activate repeatedly and boosts expire")
    void repeatedActivationsDoNotRequireTapAndExpireAtEndOfTurn() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new ElderOfLaurels());
        elder.setSummoningSick(true);
        elder.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, elder.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, elder.getId());
        harness.passBothPriorities();

        assertThat(elder.getPowerModifier()).isEqualTo(2);
        assertThat(elder.getToughnessModifier()).isEqualTo(2);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(elder.getPowerModifier()).isZero();
        assertThat(elder.getToughnessModifier()).isZero();
    }
}
