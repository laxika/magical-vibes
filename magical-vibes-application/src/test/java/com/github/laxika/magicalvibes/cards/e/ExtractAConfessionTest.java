package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtractAConfession.class, GrizzlyBears.class, HillGiant.class, SerraAngel.class})
class ExtractAConfessionTest extends BaseCardTest {

    @Test
    void withoutEvidenceOpponentChoosesCreatureToSacrifice() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent serraAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castExtractAConfession();

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(hillGiant.getId(), serraAngel.getId());
        harness.handlePermanentChosen(player2, hillGiant.getId());

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    void withEvidenceOpponentSacrificesCreatureWithGreatestPower() {
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        Card thirdEvidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence, thirdEvidence));
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new SerraAngel());
        castExtractAConfessionWithEvidence();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Extract a Confession");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstEvidence, secondEvidence, thirdEvidence);
    }

    @Test
    void withEvidenceOpponentChoosesAmongTiedGreatestPowerCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent firstAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent secondAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player2, new HillGiant());
        castExtractAConfessionWithEvidence();

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstAngel.getId(), secondAngel.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(secondAngel.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).contains(firstAngel.getId()).doesNotContain(secondAngel.getId());
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    void withEvidenceUsesPowerAtResolutionAndLeavesControllersCreaturesAlone() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new SerraAngel());
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        ownAngel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        castExtractAConfessionWithEvidence();
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    void canDeclineEvidenceEvenWhenEnoughManaValueIsAvailable() {
        Card evidence = new SerraAngel();
        Card otherEvidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evidence, otherEvidence));
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new SerraAngel());
        castExtractAConfession();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, giant.getId());

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(evidence, otherEvidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void evidenceIsPaidEvenWhenOpponentHasNoCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new SerraAngel());
        castExtractAConfessionWithEvidence();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Extract a Confession");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCollectEvidenceWithTotalManaValueGreaterThanSix() {
        harness.setGraveyard(player1, List.of(new HillGiant(), new HillGiant(), new HillGiant()));
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new SerraAngel());
        castExtractAConfessionWithEvidence();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    void cannotCollectEvidenceWithTotalManaValueBelowSix() {
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence));
        harness.setHand(player1, List.of(new ExtractAConfession()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstEvidence, secondEvidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Extract a Confession");
        assertThat(gd.stack).isEmpty();
    }

    private void castExtractAConfession() {
        harness.setHand(player1, List.of(new ExtractAConfession()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private void castExtractAConfessionWithEvidence() {
        harness.setHand(player1, List.of(new ExtractAConfession()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2));
    }
}
