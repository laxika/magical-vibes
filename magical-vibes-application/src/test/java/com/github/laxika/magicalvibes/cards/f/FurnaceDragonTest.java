package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FurnaceDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new FurnaceDragon()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("When cast from hand, all artifacts are exiled")
    void castFromHandExilesAllArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FurnaceDragon()));
        harness.addMana(player1, ManaColor.RED, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Furnace Dragon");
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Spellbook"));
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Entering from the graveyard rather than a hand cast does not exile artifacts")
    void enteringNotFromHandDoesNotExileArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.setGraveyard(player1, List.of(new FurnaceDragon()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Furnace Dragon");
        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Spellbook"));
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Spellbook"));
    }
}
