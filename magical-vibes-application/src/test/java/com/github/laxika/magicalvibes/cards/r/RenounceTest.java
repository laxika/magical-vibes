package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Renounce.class, Forest.class, FreshVolunteers.class})
class RenounceTest extends BaseCardTest {

    @Test
    @DisplayName("Gains two life for each permanent sacrificed")
    void gainsLifeForEachPermanentSacrificed() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent volunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 10);
        castRenounce();

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId(), volunteers.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Offers only permanents controlled by the spell's controller")
    void offersOnlyControllerPermanents() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownVolunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent opposingForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        castRenounce();

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(ownForest.getId(), ownVolunteers.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingForest);
    }

    @Test
    @DisplayName("Choosing no permanents gains no life")
    void choosingNoPermanentsGainsNoLife() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLife(player1, 10);
        castRenounce();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(forest.getId());
    }

    @Test
    @DisplayName("With no permanents to sacrifice, the spell needs no choice")
    void noPermanentsNeedsNoChoice() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 10);
        castRenounce();

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private void castRenounce() {
        harness.castFromHand(player1, new Renounce(), "{1}{W}");
    }
}
