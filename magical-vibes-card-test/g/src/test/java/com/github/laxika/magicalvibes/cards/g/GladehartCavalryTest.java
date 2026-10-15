package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KozileksReturn;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GladehartCavalry.class, GrizzlyBears.class, Murder.class, KozileksReturn.class})
class GladehartCavalryTest extends BaseCardTest {

    @Test
    void supportsUpToSixOtherCreatures() {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        }
        harness.setHand(player1, List.of(new GladehartCavalry()));
        addManaForCavalry();

        harness.castCreature(player1, 0, creatures.stream().map(Permanent::getId).toList());
        resolveCreatureAndEtb();

        assertThat(creatures).allSatisfy(creature ->
                assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne());
    }

    @Test
    void maySupportFewerThanSixCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GladehartCavalry()));
        addManaForCavalry();

        harness.castCreature(player1, 0, List.of(first.getId()));
        resolveCreatureAndEtb();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void gainsLifeWhenAnotherCounteredCreatureYouControlDies() {
        harness.addToBattlefield(player1, new GladehartCavalry());
        Permanent countered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);

        destroy(player1, countered);

        harness.assertLife(player1, 22);
    }

    @Test
    void doesNotGainLifeWhenCreatureWithoutPlusOneCounterDies() {
        harness.addToBattlefield(player1, new GladehartCavalry());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        destroy(player1, creature);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void gainsLifeWhenItselfDiesWithPlusOneCounter() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new GladehartCavalry());
        cavalry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);

        destroy(player1, cavalry);

        harness.assertLife(player1, 22);
    }

    @Test
    void mayChooseNoSupportTargetsEvenWhenOtherCreaturesExist() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GladehartCavalry());
        harness.setHand(player1, List.of(new GladehartCavalry()));
        addManaForCavalry();

        harness.castCreature(player1, 0, List.of());
        resolveCreatureAndEtb();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void maySupportAnOpponentsCreatureAndDoesNotSupportItself() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GladehartCavalry());
        harness.setHand(player1, List.of(new GladehartCavalry()));
        addManaForCavalry();

        harness.castCreature(player1, 0, List.of(opponentCreature.getId()));
        resolveCreatureAndEtb();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(cavalry ->
                assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void opponentsCounteredCreatureDeathOnlyGainsLifeForItsController() {
        harness.addToBattlefield(player1, new GladehartCavalry());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GladehartCavalry());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        destroy(player1, opponentCreature);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Test
    void doesNotGainLifeWhenItselfDiesWithoutPlusOneCounter() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new GladehartCavalry());
        harness.setLife(player1, 20);

        destroy(player1, cavalry);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
    @Test
    void eachCavalrySeesItselfAndTheOtherCounteredCavalryDieSimultaneously() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GladehartCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GladehartCavalry());
        for (Permanent cavalry : List.of(first, second)) {
            cavalry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            cavalry.setMarkedDamage(5);
        }
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new KozileksReturn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }
        harness.assertLife(player1, 28);
    }
    private void addManaForCavalry() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroy(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
