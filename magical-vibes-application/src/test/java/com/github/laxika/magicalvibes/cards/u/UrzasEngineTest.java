package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzasEngine.class, DeadlyInsect.class})
class UrzasEngineTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives itself banding until end of turn")
    void grantsItselfBanding() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new UrzasEngine());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, engine, Keyword.BANDING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, engine, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Second ability gives trample to the other attackers in its band only")
    void grantsTrampleToBandmates() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new UrzasEngine());
        Permanent bandmate = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());
        UUID bandId = UUID.randomUUID();
        engine.setAttacking(true);
        engine.setBandId(bandId);
        bandmate.setAttacking(true);
        bandmate.setBandId(bandId);
        loner.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, loner, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Second ability does nothing while it is not attacking in a band")
    void grantsNothingWithoutABand() {
        harness.addToBattlefield(player1, new UrzasEngine());
        Permanent bandmate = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void grantsNothingAfterSourceLeavesCombat() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new UrzasEngine());
        Permanent bandmate = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());
        UUID bandId = UUID.randomUUID();
        engine.setAttacking(true);
        engine.setBandId(bandId);
        bandmate.setAttacking(true);
        bandmate.setBandId(bandId);
        engine.setAttacking(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.TRAMPLE)).isFalse();
    }
    @Test
    void gainedBandingAllowsBandDeclaration() {
        Permanent engine = addCreatureReady(player1, new UrzasEngine());
        Permanent bandmate = addCreatureReady(player1, new DeadlyInsect());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService().declareAttackers(
                gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        assertThat(engine.getBandId()).isNotNull();
        assertThat(engine.getBandId()).isEqualTo(bandmate.getBandId());
    }
}
