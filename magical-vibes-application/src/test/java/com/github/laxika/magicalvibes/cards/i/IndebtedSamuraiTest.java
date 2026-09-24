package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.t.TakenosCavalry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndebtedSamurai.class, TakenosCavalry.class, FirstVolley.class, Frostling.class})
class IndebtedSamuraiTest extends BaseCardTest {

    private Permanent samurai() {
        return findPermanent(player1, "Indebted Samurai");
    }

    private void killWithFirstVolley(Player caster, Player targetController, String targetName) {
        harness.setHand(caster, List.of(new FirstVolley()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private void killWithFirstVolley(String targetName) {
        killWithFirstVolley(player1, player1, targetName);
    }

    @Test
    @DisplayName("Accepting the may ability puts a +1/+1 counter on it when a Samurai dies")
    void acceptingAddsCounterWhenSamuraiDies() {
        harness.addToBattlefield(player1, new IndebtedSamurai());
        harness.addToBattlefield(player1, new TakenosCavalry()); // 1/1 Samurai

        killWithFirstVolley("Takeno's Cavalry");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(samurai().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, samurai())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai())).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the may ability adds no counter")
    void decliningAddsNoCounter() {
        harness.addToBattlefield(player1, new IndebtedSamurai());
        harness.addToBattlefield(player1, new TakenosCavalry());

        killWithFirstVolley("Takeno's Cavalry");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(samurai().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A non-Samurai creature dying does not trigger it")
    void nonSamuraiDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new IndebtedSamurai());
        harness.addToBattlefield(player1, new Frostling()); // Spirit, not Samurai

        killWithFirstVolley("Frostling");

        assertThat(samurai().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Samurai controlled by an opponent does not trigger it")
    void opponentSamuraiDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new IndebtedSamurai());
        harness.addToBattlefield(player2, new TakenosCavalry());

        killWithFirstVolley(player1, player2, "Takeno's Cavalry");

        assertThat(samurai().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bushido 1 gives +1/+1 when it becomes blocked")
    void bushidoOnBecomingBlocked() {
        Permanent attacker = addCreatureReady(player1, new IndebtedSamurai());
        addCreatureReady(player2, new Frostling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 gives +1/+1 when it blocks")
    void bushidoOnBlocking() {
        addCreatureReady(player1, new Frostling());
        Permanent blocker = addCreatureReady(player2, new IndebtedSamurai());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 bonus wears off at end of turn")
    void bushidoWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new IndebtedSamurai());
        addCreatureReady(player2, new Frostling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }
}
