package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({HelmOfChatzuk.class, GrizzlyBears.class})
class HelmOfChatzukTest extends BaseCardTest {

    @Test
    @DisplayName("Grants banding to target creature until end of turn")
    void grantsBandingToTargetCreature() {
        harness.addToBattlefield(player1, new HelmOfChatzuk());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();

        // The grant wears off at end of turn.
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Can grant banding to a creature an opponent controls")
    void grantsBandingToOpponentsCreature() {
        harness.addToBattlefield(player1, new HelmOfChatzuk());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfChatzuk());
        Permanent noncreatureTarget = harness.addToBattlefieldAndReturn(player1, new HelmOfChatzuk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreatureTarget.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helm.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping the Helm is part of the activation cost")
    void tappingHelmPreventsASecondActivation() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfChatzuk());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(helm.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted banding can be used to form a band")
    void grantedBandingCanFormBand() {
        harness.addToBattlefield(player1, new HelmOfChatzuk());
        Permanent bandedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonBandedCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bandedCreature.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1, 2), null, List.of(List.of(1, 2)));

        assertThat(bandedCreature.getBandId()).isNotNull();
        assertThat(bandedCreature.getBandId()).isEqualTo(nonBandedCreature.getBandId());
    }
}
