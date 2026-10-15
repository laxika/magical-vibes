package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.s.StoneHavenMedic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalakirFamiliar.class, AngelOfMercy.class, StoneHavenMedic.class})
class MalakirFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when its controller gains life")
    void getsBoostOnControllerLifeGain() {
        Permanent familiar = addCreatureReady(player1, new MalakirFamiliar());
        castAngelOfMercy(player1);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void noBoostOnOpponentLifeGain() {
        Permanent familiar = addCreatureReady(player1, new MalakirFamiliar());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castAngelOfMercy(player2);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost lasts until end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent familiar = addCreatureReady(player1, new MalakirFamiliar());
        castAngelOfMercy(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(1);
    }

    private void castAngelOfMercy(com.github.laxika.magicalvibes.model.Player player) {
        harness.castFromHand(player, new AngelOfMercy(), "{4}{W}");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Separate life gain events each add another +1/+1")
    void separateLifeGainEventsStack() {
        Permanent familiar = addCreatureReady(player1, new MalakirFamiliar());
        addCreatureReady(player1, new StoneHavenMedic());
        addCreatureReady(player1, new StoneHavenMedic());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(2);

        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Familiar triggers independently and the boost waits for resolution")
    void eachFamiliarTriggersIndependently() {
        Permanent first = addCreatureReady(player1, new MalakirFamiliar());
        Permanent second = addCreatureReady(player1, new MalakirFamiliar());
        addCreatureReady(player1, new StoneHavenMedic());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }
}
