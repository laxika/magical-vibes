package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OkoTheTrickster.class, GrizzlyBears.class, Shock.class})
class OkoTheTricksterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts two +1/+1 counters on up to one creature you control")
    void plusOnePutsCountersOnTargetCreature() {
        Permanent oko = addReadyOko(player1, 3);
        Permanent bear = addReadyCreature(player1);
        Permanent opposingBear = addReadyCreature(player2);

        assertThatThrownBy(() -> activate(oko, 0, opposingBear.getId()))
                .isInstanceOf(IllegalStateException.class);

        activate(oko, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("0 copies a creature and prevents damage to Oko until end of turn")
    void zeroCopiesCreatureAndPreventsDamage() {
        Permanent oko = addReadyOko(player1, 4);
        Permanent bear = addReadyCreature(player1);

        activate(oko, 1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, oko)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, oko)).isFalse();
        assertThat(gqs.getEffectivePower(gd, oko)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oko)).isEqualTo(2);

        castShock(player1, oko);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oko);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.isPlaneswalker(gd, oko)).isTrue();
        castShock(player1, oko);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-7 sets your creatures to 10/10 and gives them trample until end of turn")
    void minusSevenSetsOwnCreaturesToTenTenWithTrample() {
        Permanent oko = addReadyOko(player1, 7);
        Permanent ownBear = addReadyCreature(player1);
        Permanent opposingBear = addReadyCreature(player2);

        activate(oko, 2, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.TRAMPLE)).isFalse();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isFalse();
    }

    private void activate(Permanent oko, int abilityIndex, java.util.UUID targetId) {
        harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(oko),
                abilityIndex,
                null,
                targetId);
    }

    private void castShock(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyOko(Player player, int loyalty) {
        Permanent oko = harness.addToBattlefieldAndReturn(player, new OkoTheTrickster());
        oko.setCounterCount(CounterType.LOYALTY, loyalty);
        oko.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return oko;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
