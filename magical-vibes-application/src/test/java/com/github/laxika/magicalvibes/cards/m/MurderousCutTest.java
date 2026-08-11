package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MurderousCutTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and destroys the target creature")
    void delvesAndDestroysCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<Card> graveyard = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstantWithMultipleGraveyardExile(player1, 0, targetId, List.of(0, 1, 2, 3));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Murderous Cut");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
