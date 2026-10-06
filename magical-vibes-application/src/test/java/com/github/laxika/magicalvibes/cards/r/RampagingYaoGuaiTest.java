package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
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

@CardUsed({RampagingYaoGuai.class, FountainOfYouth.class, PhyrexianArena.class, GloriousAnthem.class})
class RampagingYaoGuaiTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X counters and destroys any number of artifacts and enchantments within X")
    void entersWithCountersAndDestroysTargetsWithinX() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent arena = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        castYaoGuai(4, List.of(fountain.getId(), arena.getId()));

        Permanent yaoGuai = findPermanent(player1, "Rampaging Yao Guai");
        assertThat(yaoGuai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Fountain of Youth", "Phyrexian Arena");
    }

    @Test
    @DisplayName("Rejects target selections whose total mana value exceeds X")
    void rejectsTargetsOverTotalManaValueLimit() {
        Permanent arena = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        prepareCast(4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 4, null, null,
                List.of(arena.getId(), anthem.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");
    }

    @Test
    @DisplayName("X zero can destroy multiple zero-mana-value artifacts")
    void zeroXDestroysZeroManaValueArtifacts() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castYaoGuai(0, List.of(first.getId(), second.getId()));

        assertThat(findPermanent(player1, "Rampaging Yao Guai")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Targets with total mana value exactly X are destroyed")
    void destroysTargetsAtExactManaValueLimit() {
        Permanent arena = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castYaoGuai(6, List.of(arena.getId(), anthem.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Phyrexian Arena");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Artifacts controlled by either player can be targeted")
    void destroysOwnAndOpposingArtifacts() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castYaoGuai(0, List.of(own.getId(), opposing.getId()));

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Rampaging Yao Guai");
    }

    @Test
    @DisplayName("Counters are present before the destruction trigger resolves")
    void entersWithCountersBeforeDestruction() {
        Permanent arena = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        prepareCast(3);
        gs.playCard(gd, player1, 0, 3, null, null, List.of(arena.getId()), List.of());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rampaging Yao Guai")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Phyrexian Arena");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Phyrexian Arena");
    }

    @Test
    @DisplayName("The controller can choose no targets even when legal targets exist")
    void canChooseNoTargets() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        prepareCast(2);
        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rampaging Yao Guai")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Any number of targets includes more than ninety-nine artifacts")
    void destroysOneHundredZeroManaValueArtifacts() {
        List<java.util.UUID> targets = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId());
        }

        castYaoGuai(0, targets);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(100);
        harness.assertOnBattlefield(player1, "Rampaging Yao Guai");
    }
    private void castYaoGuai(int xValue, List<java.util.UUID> targetIds) {
        prepareCast(xValue);
        gs.playCard(gd, player1, 0, xValue, null, null, targetIds, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast(int xValue) {
        harness.setHand(player1, List.of(new RampagingYaoGuai()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
