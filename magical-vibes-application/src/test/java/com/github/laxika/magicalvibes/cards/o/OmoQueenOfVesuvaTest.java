package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({OmoQueenOfVesuva.class, Forest.class, GrizzlyBears.class})
class OmoQueenOfVesuvaTest extends BaseCardTest {

    @Test
    @DisplayName("Omo's enter-the-battlefield trigger grants everything counters and types")
    void entersAndGrantsEverythingTypes() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castOmo(List.of(forest.getId(), bears.getId()));

        assertThat(forest.getCounterCount(CounterType.EVERYTHING)).isOne();
        assertThat(gqs.effectiveLandTypes(gd, forest))
                .containsExactlyInAnyOrderElementsOf(CardSubtype.landTypes());
        assertThat(bears.getCounterCount(CounterType.EVERYTHING)).isOne();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.DRAGON)).isTrue();
    }

    @Test
    @DisplayName("Omo's attack trigger targets a land and creature independently")
    void attacksAndPutsEverythingCountersOnTargets() {
        Permanent omo = addReadyOmoWithoutTargets();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(omo)));
        harness.handlePermanentChosen(player1, forest.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(forest.getCounterCount(CounterType.EVERYTHING)).isOne();
        assertThat(bears.getCounterCount(CounterType.EVERYTHING)).isOne();
    }

    @Test
    @DisplayName("Omo's static type grants end when the counter or Omo leaves")
    void grantsAreCounterAndSourceBound() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent omo = castOmo(List.of(forest.getId(), bears.getId()));

        forest.setCounterCount(CounterType.EVERYTHING, 0);
        bears.setCounterCount(CounterType.EVERYTHING, 0);
        assertThat(gqs.effectiveLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isFalse();

        forest.setCounterCount(CounterType.EVERYTHING, 1);
        bears.setCounterCount(CounterType.EVERYTHING, 1);
        gd.playerBattlefields.get(player1.getId()).remove(omo);

        assertThat(gqs.effectiveLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    void rejectsLandAsCreatureTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new OmoQueenOfVesuva()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(forest.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castOmo(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new OmoQueenOfVesuva()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
        return findPermanent(player1, "Omo, Queen of Vesuva");
    }

    private Permanent addReadyOmoWithoutTargets() {
        return addCreatureReady(player1, new OmoQueenOfVesuva());
    }
}
