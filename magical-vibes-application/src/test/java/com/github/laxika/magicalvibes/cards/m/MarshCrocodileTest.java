package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.f.FoulFamiliar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MarshCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a blue or black creature you control to return")
    void etbOffersBlueOrBlackCreatureYouControl() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new CloudSprite()).getId();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new FoulFamiliar()).getId();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new RagingGoblin()).getId();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new CloudSprite());

        castMarshCrocodile();
        resolveUntilPermanentChoice();

        GameData gd = harness.getGameData();
        UUID crocodileId = harness.getPermanentId(player1, "Marsh Crocodile");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(blueId, blackId, crocodileId);
        assertThat(choice.validIds()).doesNotContain(greenId, redId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("ETB returns the chosen creature and makes each player discard")
    void etbReturnsChosenCreatureAndEachPlayerDiscards() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new CloudSprite()).getId();
        harness.setHand(player1, new ArrayList<>(List.of(new MarshCrocodile(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveUntilPermanentChoice();
        harness.handlePermanentChosen(player1, blueId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player1, "Cloud Sprite");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Marsh Crocodile");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castMarshCrocodile() {
        harness.setHand(player1, List.of(new MarshCrocodile()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveUntilPermanentChoice() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
