package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefensiveManeuvers.class, ElvishWarrior.class, GlorySeeker.class})
class DefensiveManeuversTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures of the chosen type get +0/+4 on every battlefield")
    void boostsAllCreaturesOfChosenType() {
        Permanent ownElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent opposingElf = addCreatureReady(player2, new ElvishWarrior());
        Permanent ownSoldier = addCreatureReady(player1, new GlorySeeker());

        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "ELF");

        assertThat(PermanentGrants.powerBoostOf(gd, ownElf)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownElf)).isEqualTo(4);
        assertThat(PermanentGrants.powerBoostOf(gd, opposingElf)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposingElf)).isEqualTo(4);
        assertThat(PermanentGrants.powerBoostOf(gd, ownSoldier)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownSoldier)).isZero();
    }

    @Test
    @CardUsed(AvianChangeling.class)
    @DisplayName("A Changeling counts as the chosen creature type")
    void changelingCountsAsChosenType() {
        Permanent changeling = addCreatureReady(player2, new AvianChangeling());
        Permanent soldier = addCreatureReady(player2, new GlorySeeker());

        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "ELF");

        assertThat(PermanentGrants.toughnessBoostOf(gd, changeling)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();
    }

    @Test
    @DisplayName("A matching creature entering after resolution is not affected")
    void doesNotBoostMatchingCreatureEnteringAfterResolution() {
        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "ELF");

        Permanent laterElf = harness.enterBattlefieldAndReturn(player2, new ElvishWarrior());

        assertThat(gqs.getEffectivePower(gd, laterElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterElf)).isEqualTo(3);
    }

    @Test
    @DisplayName("The +0/+4 modifier wears off at end of turn")
    void modifierWearsOffAtEndOfTurn() {
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());

        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "ELF");
        assertThat(PermanentGrants.toughnessBoostOf(gd, elf)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, elf)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, elf)).isZero();
    }

    @Test
    @DisplayName("A creature matches its second creature type")
    void boostsCreatureMatchingItsSecondType() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent soldier = addCreatureReady(player2, new GlorySeeker());

        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "WARRIOR");

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(7);
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();
    }

    @Test
    @DisplayName("Repeated resolutions add their toughness bonuses")
    void bonusesFromRepeatedResolutionsStack() {
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());

        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "ELF");
        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "WARRIOR");

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(11);
    }

    private void castDefensiveManeuvers(Player caster) {
        harness.castFromHand(caster, new DefensiveManeuvers(), "{3}{W}");
        harness.passBothPriorities();
    }
}
