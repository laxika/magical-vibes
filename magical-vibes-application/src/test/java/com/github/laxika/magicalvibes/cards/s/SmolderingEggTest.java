package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshmouthDragon;
import com.github.laxika.magicalvibes.cards.f.FestivalCrasher;
import com.github.laxika.magicalvibes.cards.l.LightUpTheNight;
import com.github.laxika.magicalvibes.cards.p.PlayWithFire;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmolderingEgg.class, AshmouthDragon.class, Pyroclasm.class, Shock.class,
        FestivalCrasher.class, LightUpTheNight.class, PlayWithFire.class})
class SmolderingEggTest extends BaseCardTest {

    @Test
    @DisplayName("Puts ember counters equal to mana spent on an instant or sorcery")
    void putsCountersEqualToManaSpent() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.EMBER)).isEqualTo(2);
        assertThat(egg.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Removes ember counters and transforms at seven counters")
    void transformsAtSevenCounters() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        egg.setCounterCount(CounterType.EMBER, 6);
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(egg.isTransformed()).isTrue();
        assertThat(egg.getCard()).isInstanceOf(AshmouthDragon.class);
        assertThat(egg.getCounterCount(CounterType.EMBER)).isZero();
    }

    @Test
    @DisplayName("Ashmouth Dragon deals 2 damage to a target when an instant or sorcery is cast")
    void backFaceDealsDamageToAnyTarget() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        dragon.setCard(dragon.getOriginalCard().getBackFaceCard());
        dragon.setTransformed(true);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void includesManaPaidForXAndTransformsBeforeSpellResolves() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        egg.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new LightUpTheNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castAndResolveSorcery(player1, 0, 6, player2.getId());

        assertThat(egg.isTransformed()).isTrue();
        assertThat(egg.getCounterCount(CounterType.EMBER)).isZero();
        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    void creatureSpellDoesNotAddEmberCounters() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        harness.setHand(player1, List.of(new FestivalCrasher()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.EMBER)).isZero();
        assertThat(egg.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Festival Crasher");
    }

    @Test
    void opponentsInstantDoesNotTriggerEgg() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, egg.getId());

        assertThat(egg.getCounterCount(CounterType.EMBER)).isZero();
        assertThat(egg.isTransformed()).isFalse();
        assertThat(egg.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void olderEggTriggerCannotTransformDragonBack() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        egg.setCounterCount(CounterType.EMBER, 6);
        harness.setHand(player1, List.of(new LightUpTheNight(), new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 6, player2.getId());
        harness.castInstant(player1, 0, egg.getId());
        harness.passBothPriorities();
        assertThat(egg.isTransformed()).isTrue();
        assertThat(egg.getCounterCount(CounterType.EMBER)).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(egg.isTransformed()).isTrue();
        assertThat(egg.getCard()).isInstanceOf(AshmouthDragon.class);
        assertThat(egg.getCounterCount(CounterType.EMBER)).isZero();
    }

    @Test
    void dragonCanDamageCreatureAndDoesNotAddEmberCounters() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SmolderingEgg());
        dragon.setCard(dragon.getOriginalCard().getBackFaceCard());
        dragon.setTransformed(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dragon.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(dragon.getCounterCount(CounterType.EMBER)).isZero();
        assertThat(dragon.isTransformed()).isTrue();
    }
}
