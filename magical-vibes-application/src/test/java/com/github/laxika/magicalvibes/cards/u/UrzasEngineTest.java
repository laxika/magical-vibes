package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RotWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new RotWolf());
        UUID bandId = UUID.randomUUID();
        engine.setAttacking(true);
        engine.setBandId(bandId);
        bears.setAttacking(true);
        bears.setBandId(bandId);
        loner.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, loner, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Second ability does nothing while it is not attacking in a band")
    void grantsNothingWithoutABand() {
        harness.addToBattlefield(player1, new UrzasEngine());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }
}
