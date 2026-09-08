package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepopulateTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles all creature cards from the target player's graveyard into their library")
    void shufflesCreatureCardsFromTargetGraveyard() {
        GrizzlyBears creature1 = new GrizzlyBears();
        GrizzlyBears creature2 = new GrizzlyBears();
        LightningBolt noncreature = new LightningBolt();
        harness.setGraveyard(player2, List.of(creature1, noncreature, creature2));
        harness.setHand(player1, List.of(new Repopulate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 2)
                .contains(creature1, creature2);
    }

    @Test
    @DisplayName("Only a player can be targeted")
    void rejectsPermanentTarget() {
        UUID permanentId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Repopulate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Repopulate and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Repopulate()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Repopulate");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
