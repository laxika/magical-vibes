package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HonorTroll.class})
class HonorTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking does not tap Honor Troll")
    void vigilanceKeepsAttackerUntapped() {
        Permanent troll = addCreatureReady(player1, new HonorTroll());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(troll.isTapped()).isFalse();
        assertThat(troll.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Gets +2/+1 at 25 life and loses it below 25")
    void thresholdBoost() {
        harness.addToBattlefield(player1, new HonorTroll());
        Permanent troll = findPermanent(player1, "Honor Troll");

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);

        harness.setLife(player1, 25);
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(4);

        harness.setLife(player1, 24);
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds one life to each positive life-gain event")
    void addsOneLifeToGainEvent() {
        harness.addToBattlefield(player1, new HonorTroll());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Does not modify the opponent's life gain")
    void doesNotModifyOpponentsLifeGain() {
        harness.addToBattlefield(player1, new HonorTroll());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Multiple Trolls each add one life to each gain event")
    void multipleTrollsModifyEachEventOnce() {
        harness.addToBattlefield(player1, new HonorTroll());
        harness.addToBattlefield(player1, new HonorTroll());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.assertLife(player1, 25);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Gaining zero life does not become a positive gain")
    void zeroLifeGainIsNotIncreased() {
        harness.addToBattlefield(player1, new HonorTroll());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The extra life can cross the threshold and boosts only its controller's Troll")
    void replacementGainEnablesOnlyControllersBoost() {
        harness.addToBattlefield(player1, new HonorTroll());
        harness.addToBattlefield(player2, new HonorTroll());
        harness.setLife(player1, 23);
        harness.setLife(player2, 24);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));

        harness.assertLife(player1, 25);
        Permanent ownTroll = findPermanent(player1, "Honor Troll");
        Permanent opposingTroll = findPermanent(player2, "Honor Troll");
        assertThat(gqs.getEffectivePower(gd, ownTroll)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownTroll)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingTroll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingTroll)).isEqualTo(3);
    }
}
