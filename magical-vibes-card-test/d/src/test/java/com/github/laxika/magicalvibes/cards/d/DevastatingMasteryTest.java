package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BolassCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.cards.t.TeachByExample;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevastatingMastery.class, AirElemental.class, GrizzlyBears.class, HillGiant.class,
        Plains.class, LetterOfAcceptance.class, SpinedKarok.class, BolassCitadel.class, TeachByExample.class})
class DevastatingMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting destroys all nonland permanents")
    void normalCastDestroysNonlands() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new DevastatingMastery()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Alternate casting lets the opponent return up to two nonlands before destroying the rest")
    void alternateCastOpponentChoosesReturnsBeforeDestruction() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new DevastatingMastery()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).contains(firstOpponentCreature.getId(), secondOpponentCreature.getId());
        assertThat(choice.validIds()).doesNotContain(ownCreature.getId());

        harness.handleMultiplePermanentsChosen(player2,
                List.of(firstOpponentCreature.getId(), secondOpponentCreature.getId()));

        harness.assertInHand(player2, "Hill Giant");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Alternate casting allows the opponent to return no permanents")
    void alternateCastMayReturnNone() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new DevastatingMastery()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of());

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotInHand(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Opponent may return just one artifact and the remaining nonlands are destroyed")
    void alternateCastMayReturnOneArtifact() {
        harness.addToBattlefield(player1, new LetterOfAcceptance());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        harness.addToBattlefield(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new DevastatingMastery()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        harness.assertInHand(player2, "Letter of Acceptance");
        harness.assertNotInGraveyard(player2, "Letter of Acceptance");
        harness.assertNotOnBattlefield(player2, "Letter of Acceptance");
        harness.assertInGraveyard(player1, "Letter of Acceptance");
        harness.assertInGraveyard(player2, "Spined Karok");
        harness.assertInGraveyard(player1, "Devastating Mastery");
    }

    @Test
    @DisplayName("Alternate casting still destroys nonlands when the opponent controls only lands")
    void alternateCastWithNoEligibleOpponentPermanents() {
        harness.addToBattlefield(player1, new SpinedKarok());
        harness.addToBattlefield(player1, new LetterOfAcceptance());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new DevastatingMastery()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spined Karok");
        harness.assertInGraveyard(player1, "Letter of Acceptance");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player1, "Devastating Mastery");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Paying life through Bolas's Citadel does not enable the opponent's return choice")
    void payingLifeInsteadOfManaDoesNotEnableReturnChoice() {
        harness.addToBattlefield(player1, new BolassCitadel());
        harness.addToBattlefield(player2, new SpinedKarok());
        harness.setLibrary(player1, List.of(new DevastatingMastery()));

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertLife(player1, 14);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Spined Karok");
        harness.assertNotInHand(player2, "Spined Karok");
        harness.assertInGraveyard(player1, "Bolas's Citadel");
        harness.assertInGraveyard(player1, "Devastating Mastery");
    }

    @Test
    @DisplayName("A copy of an alternate-cost Mastery also lets the opponent return permanents")
    void copyPreservesAlternateCostChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new TeachByExample(), new DevastatingMastery()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).contains(creature.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Spined Karok");
        harness.assertNotInGraveyard(player2, "Spined Karok");
        harness.assertInGraveyard(player1, "Devastating Mastery");
    }
}
