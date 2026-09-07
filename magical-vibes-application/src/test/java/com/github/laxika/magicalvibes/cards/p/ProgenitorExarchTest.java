package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({ProgenitorExarch.class})
class ProgenitorExarchTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X Incubator tokens, each with three +1/+1 counters")
    void entersWithXIncubatorTokens() {
        castProgenitorExarch(2);
        resolveProgenitorExarch();

        List<Permanent> incubators = findPermanents(player1, "Incubator");
        assertThat(incubators).hasSize(2);
        assertThat(incubators)
                .allSatisfy(incubator -> assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(3));
    }

    @Test
    @DisplayName("Transforms a targeted Incubator token you control")
    void transformsTargetedIncubator() {
        castProgenitorExarch(1);
        resolveProgenitorExarch();

        Permanent exarch = findPermanent(player1, "Progenitor Exarch");
        exarch.setSummoningSick(false);
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(exarch), null,
                incubator.getId());
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Rejects a target that is not an Incubator token you control")
    void rejectsNonIncubatorTarget() {
        castProgenitorExarch(1);
        resolveProgenitorExarch();

        Permanent exarch = findPermanent(player1, "Progenitor Exarch");
        exarch.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(exarch), null, exarch.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Incubator token you control");
    }

    private void castProgenitorExarch(int xValue) {
        harness.setHand(player1, List.of(new ProgenitorExarch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue * 2);
        harness.castCreature(player1, 0, xValue);
    }

    private void resolveProgenitorExarch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
