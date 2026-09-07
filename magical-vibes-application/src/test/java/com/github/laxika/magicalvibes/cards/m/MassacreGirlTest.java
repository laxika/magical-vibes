package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MassacreGirl.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
class MassacreGirlTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives each other creature -1/-1 and leaves Massacre Girl unchanged")
    void etbWeakensOtherCreatures() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        castMassacreGirl();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(1);

        Permanent massacreGirl = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MassacreGirl)
                .findFirst()
                .orElseThrow();
        assertThat(massacreGirl.getPowerModifier()).isZero();
        assertThat(massacreGirl.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each creature death causes another -1/-1 trigger")
    void creatureDeathsCauseCascadingWeakness() {
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());

        castMassacreGirl();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(wizard.getCard(), bears.getCard(), hillGiant.getCard());
        Permanent massacreGirl = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MassacreGirl)
                .findFirst()
                .orElseThrow();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(massacreGirl);
    }

    @Test
    @DisplayName("The -1/-1 effects wear off at end of turn")
    void weaknessWearsOffAtEndOfTurn() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        castMassacreGirl();
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(2);
    }

    private void castMassacreGirl() {
        harness.setHand(player1, List.of(new MassacreGirl()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
