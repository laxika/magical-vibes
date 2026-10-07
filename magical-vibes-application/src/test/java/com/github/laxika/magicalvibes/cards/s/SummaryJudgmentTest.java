package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusLocket;
import com.github.laxika.magicalvibes.cards.d.Doublecast;
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

@CardUsed({SummaryJudgment.class, SenateCourier.class, AzoriusLocket.class, Doublecast.class})
class SummaryJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a tapped creature during your main phase")
    void addendumDealsFiveDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Senate Courier");
    }

    @Test
    @DisplayName("Deals 3 damage to a tapped creature outside your main phase")
    void normalEffectDealsThreeDamage() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(findPermanent(player2, "Senate Courier").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Fizzles if the target becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Senate Courier").getMarkedDamage()).isZero();
    }

    @Test
    void addendumAppliesDuringPostcombatMainPhaseToOwnCreature() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SenateCourier());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Senate Courier");
    }

    @Test
    void opponentsMainPhaseDoesNotEnableAddendum() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Senate Courier");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotTargetTappedNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzoriusLocket());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void uncastCopyDoesNotReceiveAddendumBonusDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Doublecast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Senate Courier");
        assertThat(target.getMarkedDamage()).isEqualTo(3);

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Senate Courier");
    }
}
