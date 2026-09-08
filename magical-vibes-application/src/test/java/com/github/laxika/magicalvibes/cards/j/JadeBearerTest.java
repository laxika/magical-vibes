package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkSpy;
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

class JadeBearerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another Merfolk you control")
    void etbPutsCounterOnAnotherMerfolk() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkSpy());
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, List.of(merfolk.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Merfolk creature you control")
    void cannotTargetNonMerfolk() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another Merfolk creature you control");
    }

    @Test
    @DisplayName("Cannot target an opponent's Merfolk")
    void cannotTargetOpponentMerfolk() {
        UUID merfolkId = harness.addToBattlefieldAndReturn(player2, new MerfolkSpy()).getId();
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(merfolkId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another Merfolk creature you control");
    }

    @Test
    @DisplayName("Can enter without an ETB target when no other Merfolk is available")
    void canEnterWithoutTarget() {
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
