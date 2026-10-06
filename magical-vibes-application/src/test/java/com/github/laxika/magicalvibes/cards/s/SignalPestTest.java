package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SignalPest.class, AvenFisher.class, GiantSpider.class, GrizzlyBears.class})
class SignalPestTest extends BaseCardTest {

    @Test
    @DisplayName("Signal Pest cannot be blocked by a normal creature")
    void cannotBeBlockedByNormalCreature() {
        Permanent pest = attackingPest();
        gd.playerBattlefields.get(player1.getId()).add(pest);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or reach");
    }

    @Test
    @DisplayName("Signal Pest can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent pest = attackingPest();
        gd.playerBattlefields.get(player1.getId()).add(pest);

        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new AvenFisher());
        flyer.setSummoningSick(false);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Signal Pest can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        Permanent pest = attackingPest();
        gd.playerBattlefields.get(player1.getId()).add(pest);

        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setSummoningSick(false);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Signal Pest's battle cry triggers when attacking")
    void battleCryTriggersOnAttack() {
        Permanent pest = harness.addToBattlefieldAndReturn(player1, new SignalPest());
        pest.setSummoningSick(false);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Signal Pest");
    }

    @Test
    @DisplayName("Signal Pest's battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent pest = harness.addToBattlefieldAndReturn(player1, new SignalPest());
        pest.setSummoningSick(false);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(3); // 2 base + 1 battle cry
    }

    @Test
    @DisplayName("Signal Pest does not get its own battle cry boost")
    void battleCryDoesNotBoostSelf() {
        Permanent pest = harness.addToBattlefieldAndReturn(player1, new SignalPest());
        pest.setSummoningSick(false);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        // Signal Pest (0/1) should NOT boost itself
        assertThat(pest.getPowerModifier()).isEqualTo(0);
        assertThat(pest.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void battleCryDoesNotBoostNonattackingCreatures() {
        Permanent pest = harness.addToBattlefieldAndReturn(player1, new SignalPest());
        pest.setSummoningSick(false);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent nonattacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
    }

    @Test
    void multipleBattleCryTriggersBoostEachOtherButNotTheirOwnSource() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SignalPest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SignalPest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setSummoningSick(false);
        second.setSummoningSick(false);
        bears.setSummoningSick(false);

        declareAttackers(List.of(0, 1, 2));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    void battleCryResolvesAfterSourceLeavesBattlefield() {
        Permanent pest = harness.addToBattlefieldAndReturn(player1, new SignalPest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        pest.setSummoningSick(false);
        bears.setSummoningSick(false);

        declareAttackers(List.of(0, 1));
        gd.playerBattlefields.get(player1.getId()).remove(pest);
        gd.playerGraveyards.get(player1.getId()).add(pest.getCard());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isZero();
    }

    private Permanent attackingPest() {
        Permanent pest = new Permanent(new SignalPest());
        pest.setSummoningSick(false);
        pest.setAttacking(true);
        return pest;
    }
}
