package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarbedBloodletterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control and grants it wither")
    void entersAttachedAndGrantsWither() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBloodletter(creature);

        Permanent bloodletter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BarbedBloodletter)
                .findFirst()
                .orElseThrow();
        assertThat(bloodletter.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("Granted wither makes combat damage apply -1/-1 counters")
    void grantedWitherDealsMinusCounters() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        castBloodletter(attacker);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Granted wither wears off at end of turn")
    void grantedWitherWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBloodletter(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarbedBloodletter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBloodletter(Permanent target) {
        harness.setHand(player1, List.of(new BarbedBloodletter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
