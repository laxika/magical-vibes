package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ValorsReachTagTeam;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfKylem.class, ValorsReachTagTeam.class, GrizzlyBears.class})
class InvasionOfKylemTest extends BaseCardTest {

    @Test
    void entersAndBoostsUpToTwoCreatures() {
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new InvasionOfKylem()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
