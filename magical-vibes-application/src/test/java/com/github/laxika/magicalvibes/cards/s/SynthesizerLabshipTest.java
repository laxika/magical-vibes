package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SynthesizerLabship.class, GrizzlyBears.class, DarksteelCitadel.class})
class SynthesizerLabshipTest extends BaseCardTest {

    @Test
    @DisplayName("Station puts charge counters equal to the tapped creature's power on the Labship")
    void stationUsesTappedCreaturePower() {
        Permanent labship = harness.addToBattlefieldAndReturn(player1, new SynthesizerLabship());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(labship), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(labship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two charge counters enable the beginning-of-combat artifact animation")
    void animatesAnotherArtifactAtBeginningOfCombat() {
        Permanent labship = harness.addToBattlefieldAndReturn(player1, new SynthesizerLabship());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        labship.setCounterCount(CounterType.CHARGE, 2);

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId());
        assertThat(choice.validIds()).doesNotContain(labship.getId(), opponentArtifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.FLYING)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The beginning-of-combat ability is inactive below two charge counters")
    void beginningOfCombatAbilityIsInactiveBelowThreshold() {
        Permanent labship = harness.addToBattlefieldAndReturn(player1, new SynthesizerLabship());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        labship.setCounterCount(CounterType.CHARGE, 1);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    @DisplayName("Nine charge counters grant flying and vigilance")
    void nineChargeCountersGrantFlyingAndVigilance() {
        Permanent labship = harness.addToBattlefieldAndReturn(player1, new SynthesizerLabship());

        assertThat(gqs.hasKeyword(gd, labship, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, labship, Keyword.VIGILANCE)).isFalse();

        labship.setCounterCount(CounterType.CHARGE, 9);

        assertThat(gqs.hasKeyword(gd, labship, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, labship, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Station requires another untapped creature")
    void stationRequiresAnotherUntappedCreature() {
        Permanent labship = harness.addToBattlefieldAndReturn(player1, new SynthesizerLabship());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(labship), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
