package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FurnaceDragon.class, DarksteelCitadel.class, CrazedGoblin.class, BeaconOfUnrest.class})
class FurnaceDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new DarksteelCitadel());
        }
        harness.setHand(player1, List.of(new FurnaceDragon()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new DarksteelCitadel());
        }
        harness.setHand(player1, List.of(new FurnaceDragon()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity counts artifacts rather than other permanents")
    void affinityCountsArtifactsRatherThanOtherPermanents() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new CrazedGoblin());
        }
        harness.setHand(player1, List.of(new FurnaceDragon()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("When cast from hand, all artifacts are exiled")
    void castFromHandExilesAllArtifacts() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.addToBattlefield(player2, new CrazedGoblin());
        harness.setHand(player1, List.of(new FurnaceDragon()));
        harness.addMana(player1, ManaColor.RED, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Furnace Dragon");
        harness.assertNotOnBattlefield(player1, "Darksteel Citadel");
        harness.assertNotOnBattlefield(player2, "Darksteel Citadel");
        harness.assertOnBattlefield(player2, "Crazed Goblin");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Darksteel Citadel"));
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Darksteel Citadel"));
    }

    @Test
    @DisplayName("Entering without being cast from hand does not exile artifacts")
    void enteringNotFromHandDoesNotExileArtifacts() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player2, new DarksteelCitadel());
        FurnaceDragon target = new FurnaceDragon();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Furnace Dragon");
        harness.assertOnBattlefield(player1, "Darksteel Citadel");
        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Darksteel Citadel"));
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Darksteel Citadel"));
    }
}
