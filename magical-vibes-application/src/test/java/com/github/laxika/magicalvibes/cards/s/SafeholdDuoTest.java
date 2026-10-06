package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SafeholdDuo.class, GrizzlyBears.class, EliteVanguard.class, FugitiveWizard.class,
        SafeholdElite.class})
class SafeholdDuoTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SafeholdDuo());
    }

    private void castGreenSpell() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
    }

    private void castWhiteSpell() {
        harness.castFromHand(player1, new EliteVanguard(), "{W}");
    }

    private Permanent duo() {
        return findPermanent(player1, "Safehold Duo");
    }

    @Test
    @DisplayName("Casting a green spell gives Safehold Duo +1/+1 until end of turn")
    void greenSpellBoosts() {
        castGreenSpell();
        harness.passBothPriorities(); // resolve the trigger

        assertThat(duo().getPowerModifier()).isEqualTo(1);
        assertThat(duo().getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a white spell gives Safehold Duo vigilance until end of turn")
    void whiteSpellGrantsVigilance() {
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();

        castWhiteSpell();
        harness.passBothPriorities(); // resolve the trigger

        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Casting a non-green, non-white spell does not trigger either ability")
    void otherColorDoesNotTrigger() {
        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        assertThat(duo().getPowerModifier()).isEqualTo(0);
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOff() {
        castGreenSpell();
        harness.passBothPriorities();
        assertThat(duo().getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(duo().getPowerModifier()).isEqualTo(0);
        assertThat(duo().getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void vigilanceWearsOff() {
        castWhiteSpell();
        harness.passBothPriorities();
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void hybridSpellPaidWithGreenTriggersBothAbilities() {
        assertHybridSpellTriggersBothAbilities(ManaColor.GREEN);
    }

    @Test
    void hybridSpellPaidWithWhiteTriggersBothAbilities() {
        assertHybridSpellTriggersBothAbilities(ManaColor.WHITE);
    }

    private void assertHybridSpellTriggersBothAbilities(ManaColor payment) {
        harness.setHand(player1, List.of(new SafeholdElite()));
        harness.addMana(player1, payment, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        assertThat(duo().getPowerModifier()).isZero();
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(duo().getPowerModifier()).isEqualTo(1);
        assertThat(duo().getToughnessModifier()).isEqualTo(1);
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(duo().getPowerModifier()).isZero();
        assertThat(duo().getToughnessModifier()).isZero();
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void opponentsGreenWhiteSpellDoesNotTrigger() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SafeholdElite()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(duo().getPowerModifier()).isZero();
        assertThat(duo().getToughnessModifier()).isZero();
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void repeatedGreenSpellsGiveCumulativeBoosts() {
        castGreenSpell();
        harness.passBothPriorities();
        harness.passBothPriorities();

        castGreenSpell();
        harness.passBothPriorities();

        assertThat(duo().getPowerModifier()).isEqualTo(2);
        assertThat(duo().getToughnessModifier()).isEqualTo(2);
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();
    }
}
