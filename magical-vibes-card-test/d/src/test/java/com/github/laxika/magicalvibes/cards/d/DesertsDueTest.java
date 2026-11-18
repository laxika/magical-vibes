package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesertsDue.class, DesertOfTheFervent.class, DuskdaleWurm.class, FountainOfYouth.class})
class DesertsDueTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -2/-2 when the spell controller controls no Deserts")
    void givesBaseMinusTwoMinusTwo() {
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());
        castDesertsDue(target);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(-2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(-2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gives an additional -1/-1 for each Desert controlled by the spell controller")
    void scalesWithDesertsControlledBySpellController() {
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());
        castDesertsDue(target);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(-4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(-4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());
        castDesertsDue(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, java.util.List.of(new DesertsDue()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void ignoresOpponentsDesertsAndCanTargetOwnCreature() {
        harness.addToBattlefield(player2, new DesertOfTheFervent());
        Permanent target = addCreatureReady(player1, new DuskdaleWurm());

        castDesertsDue(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void countsDesertsAtResolutionAndDoesNotRecalculateAfterward() {
        Permanent initialDesert = harness.addToBattlefieldAndReturn(player1, new DesertOfTheFervent());
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());
        harness.setHand(player1, java.util.List.of(new DesertsDue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(initialDesert);
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.addToBattlefield(player1, new DesertOfTheFervent());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void putsCreatureWithZeroToughnessIntoGraveyard() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new DesertOfTheFervent());
        }
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());

        castDesertsDue(target);

        harness.assertNotOnBattlefield(player2, "Duskdale Wurm");
        harness.assertInGraveyard(player2, "Duskdale Wurm");
    }

    private void castDesertsDue(Permanent target) {
        harness.setHand(player1, java.util.List.of(new DesertsDue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
