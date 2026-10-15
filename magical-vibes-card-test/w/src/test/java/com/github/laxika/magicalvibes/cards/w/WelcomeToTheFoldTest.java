package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WelcomeToTheFold.class, AirElemental.class, GrizzlyBears.class,
        RavensCrime.class, QuilledWolf.class})
class WelcomeToTheFoldTest extends BaseCardTest {

    private void discardViaRavensCrime() {
        WelcomeToTheFold welcome = new WelcomeToTheFold();
        harness.setHand(player1, List.of(welcome));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
    }

    @Test
    void normalCastTargetsCreatureWithToughnessAtMostTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        WelcomeToTheFold welcome = new WelcomeToTheFold();
        harness.setHand(player1, List.of(welcome));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getCard().getId()));
    }

    @Test
    void normalCastCanTargetCreatureWithToughnessAboveTwoButDoesNotGainControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WelcomeToTheFold()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Welcome to the Fold");
    }

    @Test
    void madnessXUsesAnnouncedValueForToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AlternateCastXValueChoice.class);
        harness.handleXValueChosen(player1, 4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getCard().getId()));
    }

    @Test
    void toughnessIncreaseInResponsePreventsGainingControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        harness.setHand(player1, List.of(new WelcomeToTheFold()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Quilled Wolf");
        harness.assertNotOnBattlefield(player1, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Welcome to the Fold");
    }

    @Test
    void toughnessIncreaseAfterResolutionDoesNotEndControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        harness.setHand(player1, List.of(new WelcomeToTheFold()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Quilled Wolf");
        harness.assertNotOnBattlefield(player2, "Quilled Wolf");
    }

    @Test
    void madnessWithZeroXCanTargetCreatureButDoesNotUseNormalThreshold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Welcome to the Fold");
    }

    @Test
    void decliningMadnessPutsCardInGraveyard() {
        discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Welcome to the Fold");
        harness.assertNotInHand(player1, "Welcome to the Fold");
        assertThat(gd.stack).isEmpty();
    }
}
