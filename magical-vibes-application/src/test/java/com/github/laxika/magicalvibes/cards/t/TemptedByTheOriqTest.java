package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemptedByTheOriqTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of an eligible creature")
    void gainsPermanentControlOfCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTemptedByTheOriq(List.of(bear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.isStolenUntilEndOfTurn(bear.getId())).isFalse();
    }

    @Test
    @DisplayName("Can gain permanent control of an eligible planeswalker")
    void gainsPermanentControlOfPlaneswalker() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);

        castTemptedByTheOriq(List.of(jace.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jace);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(jace);
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        castTemptedByTheOriq(List.of());

        harness.assertInGraveyard(player1, "Tempted by the Oriq");
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value greater than three")
    void cannotTargetPermanentWithManaValueGreaterThanThree() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThatThrownBy(() -> castTemptedByTheOriq(List.of(giant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target your own creature")
    void cannotTargetOwnCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> castTemptedByTheOriq(List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose two permanents controlled by the same opponent")
    void cannotChooseTwoPermanentsControlledBySameOpponent() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castTemptedByTheOriq(List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    private void castTemptedByTheOriq(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new TemptedByTheOriq()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
