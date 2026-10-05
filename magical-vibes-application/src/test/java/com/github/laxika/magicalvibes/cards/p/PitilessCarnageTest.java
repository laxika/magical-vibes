package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BloodArtist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PitilessCarnage.class, Forest.class, GrizzlyBears.class, Island.class, BloodArtist.class})
class PitilessCarnageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices any number of your permanents and draws that many cards")
    void sacrificesSelectedPermanentsAndDrawsPerPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new Forest());
        setupLibrary();
        castPitilessCarnage();

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), land.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), land.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentPermanent);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing no permanents draws no cards")
    void choosingNoPermanentsDrawsNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupLibrary();
        castPitilessCarnage();

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
    }

    @Test
    void emptyBattlefieldDrawsNothingWithoutPrompting() {
        setupLibrary();
        castPitilessCarnage();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Pitiless Carnage");
    }

    @Test
    void canSacrificeOnlySomePermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        setupLibrary();
        castPitilessCarnage();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void sacrificedBloodArtistsSeeEachOtherDieSimultaneously() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BloodArtist());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BloodArtist());
        setupLibrary();
        castPitilessCarnage();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        for (int i = 0; i < 4; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void plotsForThreeManaAndCastsForFreeOnALaterTurn() {
        PitilessCarnage carnage = new PitilessCarnage();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        setupLibrary();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(carnage));
        harness.addMana(player1, ManaColor.BLACK, 2);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(carnage);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, carnage.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, carnage.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Pitiless Carnage");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(carnage);
    }

    private void castPitilessCarnage() {
        harness.setHand(player1, List.of(new PitilessCarnage()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
    }
}
