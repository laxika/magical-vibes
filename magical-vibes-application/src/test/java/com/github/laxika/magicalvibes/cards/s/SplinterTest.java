package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Splinter.class, Shuko.class, TendoIceBridge.class, Frostling.class})
class SplinterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target artifact and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        harness.addToBattlefield(player2, new Shuko());
        harness.setHand(player2, List.of(new Shuko(), new TendoIceBridge()));
        harness.setGraveyard(player2, List.of(new Shuko(), new TendoIceBridge()));

        harness.setLibrary(player2, List.of(new Shuko(), new TendoIceBridge()));

        harness.setHand(player1, List.of(new Splinter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Shuko");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Shuko");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Shuko"))
                .hasSize(4);
        harness.assertNotInHand(player2, "Shuko");
        harness.assertNotInGraveyard(player2, "Shuko");
        harness.assertInHand(player2, "Tendo Ice Bridge");
        harness.assertInGraveyard(player2, "Tendo Ice Bridge");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Shuko"))
                .anyMatch(c -> c.getName().equals("Tendo Ice Bridge"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Tendo Ice Bridge"));
    }

    @Test
    @DisplayName("Searches only the target artifact controller's zones")
    void searchesOnlyTargetControllerZones() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Shuko()).getId();
        UUID otherBattlefieldCopyId = harness.addToBattlefieldAndReturn(player2, new Shuko()).getId();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Splinter(), new Shuko()));
        harness.setGraveyard(player1, List.of(new Shuko()));
        harness.setLibrary(player1, List.of(new Shuko()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        assertThat(findPermanents(player2, "Shuko"))
                .extracting(permanent -> permanent.getId())
                .containsExactly(otherBattlefieldCopyId);
        harness.assertInHand(player1, "Shuko");
        harness.assertInGraveyard(player1, "Shuko");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shuko"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Shuko"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Shuko"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Fizzles if the target artifact leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new Shuko());
        harness.setHand(player2, List.of(new Shuko()));
        harness.setHand(player1, List.of(new Splinter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Shuko");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Shuko");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Shuko"));
        harness.assertInGraveyard(player1, "Splinter");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifactPermanent() {
        harness.addToBattlefield(player2, new Frostling());
        harness.setHand(player1, List.of(new Splinter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID creatureId = harness.getPermanentId(player2, "Frostling");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
        .isInstanceOf(IllegalStateException.class);
    }
}
