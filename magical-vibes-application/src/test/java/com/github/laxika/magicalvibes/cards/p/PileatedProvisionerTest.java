package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.ThreeTreeMascot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PileatedProvisioner.class, ThreeTreeMascot.class})
class PileatedProvisionerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a target creature you control without flying")
    void etbPutsCounterOnTargetCreatureWithoutFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetCreatureWithFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PileatedProvisioner());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control without flying");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThreeTreeMascot());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control without flying");
    }

    @Test
    @DisplayName("Creature resolves without a counter ability on the stack when there is no legal target")
    void creatureResolvesWithoutLegalTarget() {
        harness.addToBattlefield(player1, new PileatedProvisioner());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No counter is placed if the target leaves before the ETB ability resolves")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB ability still places its counter after Provisioner leaves")
    void sourceLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PileatedProvisioner)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
