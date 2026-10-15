package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskLegionZealot;
import com.github.laxika.magicalvibes.cards.e.ExpelFromOrazca;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanguineGlorifier.class, DuskLegionZealot.class, RaptorCompanion.class, ExpelFromOrazca.class})
class SanguineGlorifierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another Vampire you control")
    void etbPutsCounterOnAnotherVampireYouControl() {
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID vampireId = harness.getPermanentId(player1, "Dusk Legion Zealot");
        harness.castCreature(player1, 0, vampireId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(vampireId))
                .findFirst().orElseThrow();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target the entering Vampire")
    void etbCannotTargetEnteringVampire() {
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent glorifier = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(glorifier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Rejects a non-Vampire as target")
    void rejectsNonVampireTarget() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID raptorId = harness.getPermanentId(player1, "Raptor Companion");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, raptorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Vampire you control");
    }

    @Test
    @DisplayName("Rejects an opponent's Vampire as target")
    void rejectsOpponentsVampire() {
        harness.addToBattlefield(player2, new DuskLegionZealot());
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID vampireId = harness.getPermanentId(player2, "Dusk Legion Zealot");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, vampireId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Vampire you control");
    }

    @Test
    void triggerStillResolvesAfterSourceLeavesBattlefield() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new DuskLegionZealot());
        harness.setHand(player1, List.of(new SanguineGlorifier(), new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, vampire.getId());
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Sanguine Glorifier");
        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.assertInHand(player1, "Sanguine Glorifier");
        harness.assertNotOnBattlefield(player1, "Sanguine Glorifier");
        harness.passBothPriorities();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerDoesNotRetargetWhenTargetLeavesBattlefield() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new DuskLegionZealot());
        Permanent otherVampire = harness.addToBattlefieldAndReturn(player1, new DuskLegionZealot());
        harness.setHand(player1, List.of(new SanguineGlorifier(), new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, vampire.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, vampire.getId());
        harness.assertInHand(player1, "Dusk Legion Zealot");
        harness.passBothPriorities();

        assertThat(otherVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving the creature puts its ETB trigger on the stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID vampireId = harness.getPermanentId(player1, "Dusk Legion Zealot");
        harness.castCreature(player1, 0, vampireId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(vampireId);
    }
}
