package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AetherChaser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarricadeBreaker.class, AetherChaser.class})
class BarricadeBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Barricade Breaker must attack each combat if able")
    void mustAttackWhenAble() {
        Permanent breaker = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        breaker.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void mayOmitTappedBreakerFromAttack() {
        Permanent breaker = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        breaker.setSummoningSick(false);
        breaker.tap();

        declareAttackers(List.of());

        assertThat(breaker.isAttacking()).isFalse();
    }

    @Test
    void mayOmitSummoningSickBreakerFromAttack() {
        Permanent breaker = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        breaker.setSummoningSick(true);

        declareAttackers(List.of());

        assertThat(breaker.isAttacking()).isFalse();
    }

    @Test
    void canDeclareBreakerAsAttacker() {
        Permanent breaker = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        breaker.setSummoningSick(false);

        declareAttackers(List.of(0));

        harness.assertLife(player2, 13);
    }

    @Test
    void improviseCanTapSummoningSickArtifactCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        artifact.setSummoningSick(true);
        harness.setHand(player1, List.of(new BarricadeBreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player1, "Barricade Breaker");
    }

    @Test
    void improviseCannotTapAlreadyTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BarricadeBreaker());
        artifact.tap();
        harness.setHand(player1, List.of(new BarricadeBreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Barricade Breaker");
    }

    @Test
    void improviseCannotTapNonartifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AetherChaser());
        harness.setHand(player1, List.of(new BarricadeBreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not an artifact");
        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Barricade Breaker");
    }

}
