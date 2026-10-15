package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiganticBigBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DancingFromDarkToDawn.class, Forest.class, GrizzlyBears.class, GiganticBigBear.class})
class DancingFromDarkToDawnTest extends BaseCardTest {

    @Test
    void putsCountersEqualToCreatureSpellsManaValueOnTargetCreature() {
        harness.addToBattlefield(player1, new DancingFromDarkToDawn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void createsABearWhenALandEnters() {
        harness.addToBattlefield(player1, new DancingFromDarkToDawn());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent -> {
            assertThat(permanent.getCard().isToken()).isTrue();
            assertThat(permanent.getCard().getName()).isEqualTo("Bear");
            assertThat(permanent.getEffectivePower()).isEqualTo(2);
            assertThat(permanent.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    void usesTheCastSpellsManaValueRatherThanTheTargetsManaValue() {
        harness.addToBattlefield(player1, new DancingFromDarkToDawn());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        Permanent bear = findPermanent(player1, "Bear");
        harness.setHand(player1, List.of(new GiganticBigBear()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(findPermanent(player1, "Gigantic Big Bear")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForAnOpponentsCreatureSpell() {
        harness.addToBattlefield(player1, new DancingFromDarkToDawn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiganticBigBear());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiganticBigBear()));
        harness.addMana(player2, ManaColor.GREEN, 7);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player2, "Gigantic Big Bear")).isEqualTo(1);
    }

    @Test
    void enteringACreatureWithoutCastingDoesNotPutCountersOnIt() {
        harness.addToBattlefield(player1, new DancingFromDarkToDawn());

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GiganticBigBear());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castingANoncreatureSpellDoesNotPutCountersOnACreature() {
        harness.addToBattlefield(player1, new DancingFromDarkToDawn());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiganticBigBear());
        harness.setHand(player1, List.of(new DancingFromDarkToDawn()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Dancing from Dark to Dawn")).isEqualTo(2);
    }

    @Test
    void aCreatureSpellCannotTargetItselfWhenNoCreatureIsOnTheBattlefield() {
        harness.addToBattlefield(player1, new DancingFromDarkToDawn());
        harness.setHand(player1, List.of(new GiganticBigBear()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Gigantic Big Bear")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void landfallTriggersForLandsPutOntoTheBattlefieldButNotOpponentsLands() {
        harness.addToBattlefield(player1, new DancingFromDarkToDawn());

        harness.enterBattlefieldAndReturn(player2, new Forest());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Bear")).isZero();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Bear")).isEqualTo(2);
    }
}
