package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AlabasterHostSanctifier;
import com.github.laxika.magicalvibes.cards.v.VanquishTheWeak;
import com.github.laxika.magicalvibes.cards.v.ValorsReachTagTeam;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfKylem.class, ValorsReachTagTeam.class, GrizzlyBears.class,
        AlabasterHostSanctifier.class, VanquishTheWeak.class})
class InvasionOfKylemTest extends BaseCardTest {

    @Test
    void entersAndBoostsUpToTwoCreatures() {
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());

        castWithTargets(List.of(firstBear.getId(), secondBear.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.HASTE)).isTrue();
    }

    @Test
    void transformedSpellCreatesWarriorsThatGrowWhenAttackingTogether() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfKylem());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        resolveAllTriggers();

        List<Permanent> warriors = findPermanents(player1, "Warrior");
        assertThat(warriors).hasSize(2);
        for (Permanent warrior : warriors) {
            assertThat(warrior.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
            assertThat(warrior.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
            assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);
            warrior.setSummoningSick(false);
        }

        List<Integer> warriorIndices = warriors.stream()
                .map(warrior -> gd.playerBattlefields.get(player1.getId()).indexOf(warrior))
                .toList();
        declareAttackers(warriorIndices);
        resolveAllTriggers();

        assertThat(warriors).allSatisfy(warrior ->
                assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    void canEnterWithoutTargets() {
        harness.castFromHand(player1, new InvasionOfKylem(), "{2}{R}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Invasion of Kylem");
        assertThat(findPermanent(player1, "Invasion of Kylem").getCounterCount(CounterType.DEFENSE))
                .isEqualTo(5);
    }

    @Test
    void canBoostOneOpposingCreatureAndEffectsExpireAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new AlabasterHostSanctifier());
        castWithTargets(List.of(creature.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void survivingTargetStillReceivesBothBenefits() {
        Permanent first = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent second = addCreatureReady(player1, new AlabasterHostSanctifier());
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        castWithTargets(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.castInstant(player2, 0, first.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }

    @Test
    void warriorDoesNotGrowWhenAttackingAlone() {
        List<Permanent> warriors = createReadyWarriors();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warriors.getFirst())));
        resolveAllTriggers();

        assertThat(warriors).allSatisfy(warrior ->
                assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void warriorDoesNotGrowWhenOnlyOtherAttackerIsNontoken() {
        List<Permanent> warriors = createReadyWarriors();
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(warriors.getFirst()),
                gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(warriors).allSatisfy(warrior ->
                assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void warriorStillGrowsWhenOtherAttackingTokenIsDestroyedInResponse() {
        List<Permanent> warriors = createReadyWarriors();
        List<Integer> indices = warriors.stream()
                .map(warrior -> gd.playerBattlefields.get(player1.getId()).indexOf(warrior)).toList();
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(indices));

        harness.castInstant(player2, 0, warriors.getFirst().getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(warriors.getFirst());
        assertThat(warriors.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castWithTargets(List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new InvasionOfKylem()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targets);
    }

    private List<Permanent> createReadyWarriors() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfKylem());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        resolveAllTriggers();
        List<Permanent> warriors = findPermanents(player1, "Warrior");
        assertThat(warriors).hasSize(2);
        warriors.forEach(warrior -> warrior.setSummoningSick(false));
        return warriors;
    }
}
