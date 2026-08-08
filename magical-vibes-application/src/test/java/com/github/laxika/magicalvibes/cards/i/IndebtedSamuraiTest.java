package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IndebtedSamuraiTest extends BaseCardTest {

    private Permanent samurai() {
        return findPermanent(player1, "Indebted Samurai");
    }

    private void killWithShock(String targetName) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve Shock -> creature dies -> death trigger onto stack
        harness.passBothPriorities(); // resolve the death trigger (MayEffect prompt)
    }

    @Test
    @DisplayName("Accepting the may ability puts a +1/+1 counter on it when a Samurai dies")
    void acceptingAddsCounterWhenSamuraiDies() {
        harness.addToBattlefield(player1, new IndebtedSamurai());
        harness.addToBattlefield(player1, new DevotedRetainer()); // 1/1 Samurai

        killWithShock("Devoted Retainer");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(samurai().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, samurai())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai())).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the may ability adds no counter")
    void decliningAddsNoCounter() {
        harness.addToBattlefield(player1, new IndebtedSamurai());
        harness.addToBattlefield(player1, new DevotedRetainer());

        killWithShock("Devoted Retainer");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(samurai().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A non-Samurai creature dying does not trigger it")
    void nonSamuraiDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new IndebtedSamurai());
        harness.addToBattlefield(player1, new GrizzlyBears()); // Bear, not Samurai

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(samurai().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bushido 1 gives +1/+1 when it becomes blocked")
    void bushidoOnBecomingBlocked() {
        Permanent attacker = addReady(player1, new IndebtedSamurai());
        attacker.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 gives +1/+1 when it blocks")
    void bushidoOnBlocking() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new IndebtedSamurai());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
