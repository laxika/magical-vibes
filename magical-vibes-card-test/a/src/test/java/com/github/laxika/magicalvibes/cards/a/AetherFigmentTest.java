package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherFigment.class, GrizzlyBears.class})
class AetherFigmentTest extends BaseCardTest {

    @Test
    void entersWithoutCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new AetherFigment()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent figment = findPermanent(player1, "Aether Figment");
        assertThat(figment).isNotNull();
        assertThat(figment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithTwoCountersWhenKicked() {
        harness.setHand(player1, List.of(new AetherFigment()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent figment = findPermanent(player1, "Aether Figment");
        assertThat(figment).isNotNull();
        assertThat(figment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotBeBlocked() {
        Permanent attacker = addCreatureReady(player1, new AetherFigment());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void cannotKickWithoutPayingTheAdditionalCost() {
        harness.setHand(player1, List.of(new AetherFigment()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersWithoutCountersWhenPutOntoBattlefieldWithoutCasting() {
        Permanent figment = harness.enterBattlefieldAndReturn(player1, new AetherFigment());

        assertThat(figment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
