package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.m.MightOfOldKrosa;
import com.github.laxika.magicalvibes.cards.s.SageOfEpityr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PendelhavenElder.class, AshcoatBear.class, SageOfEpityr.class, MightOfOldKrosa.class})
class PendelhavenElderTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures that are currently 1/1, but not other creatures or opponents' creatures")
    void boostsOnlyYourOneOneCreatures() {
        Permanent elder = addCreatureReady(player1, new PendelhavenElder());
        Permanent sage = addCreatureReady(player1, new SageOfEpityr());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());
        Permanent opponentSage = addCreatureReady(player2, new SageOfEpityr());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elder.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSage)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses current power and toughness and wears off at cleanup")
    void usesCurrentStatsAndExpires() {
        Permanent elder = addCreatureReady(player1, new PendelhavenElder());
        Permanent sage = addCreatureReady(player1, new SageOfEpityr());

        harness.setHand(player1, List.of(new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, sage.getId());

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(1);
    }

    @Test
    @DisplayName("Checks power and toughness when the ability resolves, after responses")
    void checksStatsAtResolution() {
        Permanent elder = addCreatureReady(player1, new PendelhavenElder());
        Permanent sage = addCreatureReady(player1, new SageOfEpityr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player1, 0, sage.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(5);
    }

    @Test
    @DisplayName("A second Elder activation does not boost creatures already made 2/3")
    void consecutiveActivationsDoNotBoostTheSameCreaturesTwice() {
        Permanent firstElder = addCreatureReady(player1, new PendelhavenElder());
        Permanent secondElder = addCreatureReady(player1, new PendelhavenElder());
        Permanent sage = addCreatureReady(player1, new SageOfEpityr());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(firstElder.isTapped()).isTrue();
        assertThat(secondElder.isTapped()).isTrue();
        for (Permanent creature : List.of(firstElder, secondElder, sage)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("A creature entering after resolution does not receive the boost")
    void doesNotBoostCreaturesEnteringLater() {
        Permanent elder = addCreatureReady(player1, new PendelhavenElder());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent laterElder = harness.enterBattlefieldAndReturn(player1, new PendelhavenElder());

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterElder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterElder)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Elder with summoning sickness cannot pay the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new PendelhavenElder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(elder.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Elder cannot activate again")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new PendelhavenElder());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
