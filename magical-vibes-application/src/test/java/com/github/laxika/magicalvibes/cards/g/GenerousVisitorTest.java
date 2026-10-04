package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.r.RoaringEarth;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenerousVisitor.class, RoaringEarth.class, NetworkTerminal.class, BambooGroveArcher.class})
class GenerousVisitorTest extends BaseCardTest {

    @Test
    void enchantmentSpellPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new GenerousVisitor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousVisitor());
        harness.setHand(player1, List.of(new RoaringEarth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonEnchantmentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GenerousVisitor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GenerousVisitor());
        harness.setHand(player1, List.of(new GenerousVisitor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GenerousVisitor());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        harness.setHand(player1, List.of(new RoaringEarth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantmentCreatureCastTriggersBeforeSpellResolvesAndCanTargetVisitor() {
        Permanent visitor = harness.addToBattlefieldAndReturn(player1, new GenerousVisitor());
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, visitor.getId());
        harness.passBothPriorities();

        assertThat(visitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Bamboo Grove Archer");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bamboo Grove Archer");
        assertThat(visitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsEnchantmentCastDoesNotTrigger() {
        Permanent visitor = harness.addToBattlefieldAndReturn(player1, new GenerousVisitor());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RoaringEarth()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Roaring Earth");
        assertThat(visitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enchantmentEnteringWithoutBeingCastDoesNotTrigger() {
        Permanent visitor = harness.addToBattlefieldAndReturn(player1, new GenerousVisitor());

        harness.addToBattlefield(player1, new BambooGroveArcher());
        harness.passBothPriorities();

        assertThat(visitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterVisitorLeavesBattlefield() {
        Permanent visitor = harness.addToBattlefieldAndReturn(player1, new GenerousVisitor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousVisitor());
        harness.setHand(player1, List.of(new RoaringEarth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        gd.battlefield.get(player1.getId()).remove(visitor);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggerDoesNotPutCounterOnTargetThatLeftBattlefield() {
        Permanent visitor = harness.addToBattlefieldAndReturn(player1, new GenerousVisitor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousVisitor());
        harness.setHand(player1, List.of(new RoaringEarth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        gd.battlefield.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(visitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Roaring Earth");
    }
}
