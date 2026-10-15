package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AuriokEdgewright;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({FumeSpitter.class, AuriokEdgewright.class, Memnite.class})
class FumeSpitterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Fume Spitter and puts ability on stack")
    void activatingSacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new FumeSpitter());
        harness.addToBattlefield(player2, new AuriokEdgewright());

        UUID targetId = harness.getPermanentId(player2, "Auriok Edgewright");
        harness.activateAbility(player1, 0, null, targetId);

        // Fume Spitter should be sacrificed
        harness.assertNotOnBattlefield(player1, "Fume Spitter");
        harness.assertInGraveyard(player1, "Fume Spitter");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Fume Spitter");
    }

    @Test
    @DisplayName("Puts a -1/-1 counter on target creature")
    void putsCounterOnTarget() {
        harness.addToBattlefield(player1, new FumeSpitter());
        harness.addToBattlefield(player2, new AuriokEdgewright());

        UUID targetId = harness.getPermanentId(player2, "Auriok Edgewright");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Auriok Edgewright");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills a 1/1 creature with -1/-1 counter")
    void killsOneOneCreature() {
        harness.addToBattlefield(player1, new FumeSpitter());
        harness.addToBattlefield(player2, new Memnite());

        UUID targetId = harness.getPermanentId(player2, "Memnite");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertInGraveyard(player2, "Memnite");
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new FumeSpitter());
        harness.addToBattlefield(player2, new AuriokEdgewright());

        UUID targetId = harness.getPermanentId(player2, "Auriok Edgewright");
        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick, targeting own creature")
    void activatesWhileTappedAndSummoningSick() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new FumeSpitter());
        spitter.tap();
        spitter.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AuriokEdgewright());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Fume Spitter");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Auriok Edgewright");
    }

    @Test
    @DisplayName("Can target itself, but the sacrificed target is gone at resolution")
    void canTargetItself() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new FumeSpitter());

        harness.activateAbility(player1, 0, null, spitter.getId());

        harness.assertNotOnBattlefield(player1, "Fume Spitter");
        harness.assertInGraveyard(player1, "Fume Spitter");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(spitter.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(spitter.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
