package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrotherhoodScribe.class, GrizzlyBears.class, LeoninScimitar.class, Spellbook.class})
class BrotherhoodScribeTest extends BaseCardTest {

    @Test
    void cannotActivateWithoutThreeArtifacts() {
        Permanent scribe = addReadyScribe();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
        assertThat(scribe.isTapped()).isFalse();
    }

    @Test
    void gainsEnergyAndBoostsAllOwnCreaturesUntilEndOfTurn() {
        Permanent scribe = addReadyScribe();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addArtifacts();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, scribe)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scribe)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scribe)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scribe)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void doesNotBoostWhenEnergyIsGainedDuringAnOpponentsTurn() {
        Permanent scribe = addReadyScribe();
        addArtifacts();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, scribe)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scribe)).isEqualTo(3);
    }

    private Permanent addReadyScribe() {
        Permanent scribe = harness.addToBattlefieldAndReturn(player1, new BrotherhoodScribe());
        scribe.setSummoningSick(false);
        return scribe;
    }

    private void addArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new Spellbook());
    }
}
