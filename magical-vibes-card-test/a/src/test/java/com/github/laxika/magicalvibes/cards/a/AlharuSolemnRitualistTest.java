package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlharuSolemnRitualist.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class AlharuSolemnRitualistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on each of up to two other creatures")
    void etbPutsCountersOnTwoOtherCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAlharu(List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nontoken creature with a +1/+1 counter dying creates a Spirit")
    void counteredAllyDeathCreatesSpirit() {
        harness.addToBattlefield(player1, new AlharuSolemnRitualist());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killWithShock(elves);

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    void createdSpiritHasTheRequiredCharacteristics() {
        harness.addToBattlefield(player1, new AlharuSolemnRitualist());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killWithShock(elves);

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        Permanent spirit = spirits.getFirst();
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("A creature without a +1/+1 counter dying creates no Spirit")
    void uncounteredAllyDeathCreatesNoSpirit() {
        harness.addToBattlefield(player1, new AlharuSolemnRitualist());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        killWithShock(elves);

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Alharu creates a Spirit when it dies with a +1/+1 counter")
    void selfDeathCreatesSpirit() {
        Permanent alharu = harness.addToBattlefieldAndReturn(player1, new AlharuSolemnRitualist());
        alharu.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        alharu.setMarkedDamage(4);

        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    void etbCanChooseNoTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAlharu(List.of());

        harness.assertOnBattlefield(player1, "Alharu, Solemn Ritualist");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void etbCanTargetOneOpposingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAlharu(List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void etbStillCountersRemainingTargetWhenOneTargetDies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new AlharuSolemnRitualist()));
        addAlharuMana();
        harness.castCreature(player1, 0, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void counteredOpponentDeathCreatesNoSpirit() {
        harness.addToBattlefield(player1, new AlharuSolemnRitualist());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killWithShock(elves);

        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    void counteredTokenAllyDeathCreatesNoSpirit() {
        harness.addToBattlefield(player1, new AlharuSolemnRitualist());
        LlanowarElves tokenCopy = new LlanowarElves();
        tokenCopy.setToken(true);
        Permanent elves = harness.addToBattlefieldAndReturn(player1, tokenCopy);
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killWithShock(elves);

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void counteredTokenCopyOfAlharuDoesNotTriggerForItsOwnDeath() {
        AlharuSolemnRitualist tokenCopy = new AlharuSolemnRitualist();
        tokenCopy.setToken(true);
        Permanent alharu = harness.addToBattlefieldAndReturn(player1, tokenCopy);
        alharu.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        alharu.setMarkedDamage(4);

        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void uncounteredSelfDeathCreatesNoSpirit() {
        Permanent alharu = harness.addToBattlefieldAndReturn(player1, new AlharuSolemnRitualist());
        alharu.setMarkedDamage(3);

        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void simultaneousCounteredSelfAndAllyDeathsCreateTwoSpirits() {
        Permanent alharu = harness.addToBattlefieldAndReturn(player1, new AlharuSolemnRitualist());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        alharu.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        alharu.setMarkedDamage(4);
        elves.setMarkedDamage(2);

        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    private void castAlharu(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new AlharuSolemnRitualist()));
        addAlharuMana();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addAlharuMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
