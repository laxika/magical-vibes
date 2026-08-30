package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
}
