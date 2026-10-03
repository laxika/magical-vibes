package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Biotransference;
import com.github.laxika.magicalvibes.cards.c.CanoptekWraith;
import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SisterOfSilence;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnrakyrTheTraveller.class, MindStone.class, SisterOfSilence.class,
        Biotransference.class, GrafdiggersCage.class, ThaliaGuardianOfThraben.class,
        CanoptekWraith.class, EverflowingChalice.class})
class AnrakyrTheTravellerTest extends BaseCardTest {

    @Test
    void attacksOfferArtifactFromHandForLifeEqualToManaValue() {
        harness.setLife(player1, 10);
        MindStone mindStone = new MindStone();
        harness.setHand(player1, List.of(mindStone));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mindStone);
    }

    @Test
    void attacksOfferArtifactFromGraveyardForLifeEqualToManaValue() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        MindStone mindStone = new MindStone();
        harness.setGraveyard(player1, List.of(mindStone));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(mindStone);
    }

    @Test
    void attacksDoNotOfferNonArtifactCards() {
        harness.setHand(player1, List.of(new SisterOfSilence()));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void mayDeclineWithoutPayingLifeOrMovingTheArtifact() {
        harness.setLife(player1, 10);
        MindStone mindStone = new MindStone();
        harness.setHand(player1, List.of(mindStone));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mindStone);
        harness.assertNotOnBattlefield(player1, "Mind Stone");
    }

    @Test
    void cannotPayMoreLifeThanAvailable() {
        harness.setLife(player1, 1);
        MindStone mindStone = new MindStone();
        harness.setHand(player1, List.of(mindStone));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mindStone);
    }

    @Test
    void oneAttackCastsOnlyOneArtifactAcrossBothZones() {
        harness.setLife(player1, 10);
        MindStone handCard = new MindStone();
        MindStone graveyardCard = new MindStone();
        harness.setHand(player1, List.of(handCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 8);
        assertThat(countPermanents(player1, "Mind Stone")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId()).size()
                + gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(1);
    }

    @Test
    void biotransferenceAllowsCreatureCardFromHand() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SisterOfSilence()));
        addReadyAnrakyr();
        harness.addToBattlefield(player1, new Biotransference());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sister of Silence");
        harness.assertLife(player1, 4);
    }

    @Test
    void biotransferenceAllowsCreatureCardFromGraveyard() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new SisterOfSilence()));
        addReadyAnrakyr();
        harness.addToBattlefield(player1, new Biotransference());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sister of Silence");
        harness.assertLife(player1, 4);
    }

    @Test
    void grafdiggersCagePreventsCastingFromGraveyard() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        MindStone mindStone = new MindStone();
        harness.setGraveyard(player1, List.of(mindStone));
        addReadyAnrakyr();
        harness.addToBattlefield(player2, new GrafdiggersCage());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 10);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mindStone);
        harness.assertNotOnBattlefield(player1, "Mind Stone");
    }

    @Test
    void thaliaTaxMustBePaidEvenWhenManaCostIsReplacedByLife() {
        harness.setLife(player1, 10);
        MindStone mindStone = new MindStone();
        harness.setHand(player1, List.of(mindStone));
        addReadyAnrakyr();
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mindStone);
        harness.assertNotOnBattlefield(player1, "Mind Stone");
    }

    @Test
    void canCastArtifactCreatureFromGraveyard() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        CanoptekWraith wraith = new CanoptekWraith();
        harness.setGraveyard(player1, List.of(wraith));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 7);
        harness.assertOnBattlefield(player1, "Canoptek Wraith");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wraith);
    }

    @Test
    void canCastZeroManaValueArtifactWithoutLosingLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new EverflowingChalice()));
        addReadyAnrakyr();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertOnBattlefield(player1, "Everflowing Chalice");
        assertThat(findPermanent(player1, "Everflowing Chalice").getCounterCount(CounterType.CHARGE)).isZero();
    }

    private void addReadyAnrakyr() {
        addCreatureReady(player1, new AnrakyrTheTraveller());
    }
}
