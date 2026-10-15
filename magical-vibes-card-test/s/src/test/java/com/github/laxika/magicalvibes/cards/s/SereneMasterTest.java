package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BraveTheSands;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SereneMaster.class, GiantSpider.class, BraveTheSands.class})
class SereneMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking targets the creature being blocked and exchanges power")
    void exchangesPowerWithBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent master = addCreatureReady(player2, new SereneMaster());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, master), indexOf(player1, attacker))));
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature that Serene Master is not blocking")
    void cannotTargetUnblockedCreature() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent unrelated = addCreatureReady(player1, new GiantSpider());
        unrelated.setAttacking(true);
        Permanent master = addCreatureReady(player2, new SereneMaster());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, master), indexOf(player1, attacker))));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, unrelated.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The exchanged powers revert at end of combat")
    void exchangeEndsWithCombat() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent master = addCreatureReady(player2, new SereneMaster());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, master), indexOf(player1, attacker))));
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isZero();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @CardUsed({BraveTheSands.class})
    @DisplayName("Blocking multiple creatures triggers the exchange only once")
    void multipleBlocksExchangePowerOnlyOnce() {
        Permanent first = addCreatureReady(player1, new GiantSpider());
        Permanent second = addCreatureReady(player1, new GiantSpider());
        first.setAttacking(true);
        second.setAttacking(true);
        Permanent master = addCreatureReady(player2, new SereneMaster());
        harness.addToBattlefield(player2, new BraveTheSands());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(indexOf(player2, master), indexOf(player1, first)),
                new BlockerAssignment(indexOf(player2, master), indexOf(player1, second))));
        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, first)).isZero();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters modify the exchanged powers after the exchange")
    void countersApplyToExchangedPowers() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent master = addCreatureReady(player2, new SereneMaster());
        master.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, master), indexOf(player1, attacker))));
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    @DisplayName("No exchange occurs if Serene Master leaves before resolution")
    void sourceLeavingPreventsExchange() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent master = addCreatureReady(player2, new SereneMaster());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, master), indexOf(player1, attacker))));
        harness.handlePermanentChosen(player2, attacker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(master);
        gd.playerGraveyards.get(player2.getId()).add(master.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
