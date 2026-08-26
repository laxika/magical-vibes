package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({OmegaHeartlessEvolution.class, EvolvingWilds.class, Forest.class, GrizzlyBears.class})
class OmegaHeartlessEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and stuns one opposing nonland permanent and gains life for each nonbasic land")
    void tapsAndStunsTargetAndGainsLifeForNonbasicLands() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new EvolvingWilds());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castOmega(List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Basic lands do not increase the stun-counter or life-gain amount")
    void countsOnlyNonbasicLands() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castOmega(List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Allows at most one target per opponent and only targets opposing nonland permanents")
    void validatesTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareCast();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");

        prepareCast();
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);

        prepareCast();
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOmega(List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new OmegaHeartlessEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
