package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitilessCarnage.class, Forest.class, GrizzlyBears.class, Island.class})
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
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Island(), new Island(), new Island(), new Island()));
    }

    private void castPitilessCarnage() {
        harness.setHand(player1, List.of(new PitilessCarnage()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
    }
}
