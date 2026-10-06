package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KozileksChanneler;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScourFromExistence.class, Forest.class, GrizzlyBears.class, KozileksChanneler.class})
class ScourFromExistenceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target permanent")
    void exilesTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile a land")
    void exilesLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Can exile a permanent controlled by the caster")
    void exilesOwnPermanent() {
        harness.addToBattlefield(player1, new KozileksChanneler());
        harness.setHand(player1, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        UUID targetId = harness.getPermanentId(player1, "Kozilek's Channeler");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Kozilek's Channeler");
        harness.assertNotInGraveyard(player1, "Kozilek's Channeler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Kozilek's Channeler"));
        harness.assertInGraveyard(player1, "Scour from Existence");
    }

    @Test
    @DisplayName("Does not resolve when its target has already been exiled")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new KozileksChanneler());
        harness.setHand(player1, List.of(new ScourFromExistence(), new ScourFromExistence()));
        harness.addMana(player1, ManaColor.COLORLESS, 14);

        UUID targetId = harness.getPermanentId(player2, "Kozilek's Channeler");
        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kozilek's Channeler");
        harness.assertNotInGraveyard(player2, "Kozilek's Channeler");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Kozilek's Channeler"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Scour from Existence"))
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
