package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DawningAngel;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfVitality.class, DawningAngel.class, TurnToFrog.class})
class AngelOfVitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 at 25 life and not below 25")
    void thresholdBoost() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new AngelOfVitality());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);

        harness.setLife(player1, 25);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);

        harness.setLife(player1, 24);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds one life to each positive life-gain event")
    void addsOneLifeToGainEvent() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Two Angels add two life to one life-gain event")
    void multipleAngelsStackAdditively() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("A life gain by the opponent is not increased")
    void doesNotModifyOpponentsLifeGain() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Increasing a life total is also modified")
    void modifiesLifeTotalIncrease() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applySetLifeTotal(gd, player1.getId(), 23));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("An entering Dawning Angel gains five life and activates the boost")
    void enteringAngelGainActivatesBoost() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new AngelOfVitality());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new DawningAngel(), "{4}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 25);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gaining zero life does not gain an additional life")
    void zeroLifeGainIsNotIncreased() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Separate life gain events each receive an additional life")
    void separateGainsAreEachIncreased() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        });

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Losing all abilities stops the additional life gain")
    void abilityRemovalStopsReplacement() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new AngelOfVitality());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, angel.getId());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 23);
    }
}
