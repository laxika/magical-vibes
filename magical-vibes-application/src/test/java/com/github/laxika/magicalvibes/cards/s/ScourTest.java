package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Compost;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scour.class, Compost.class, MetathranSoldier.class})
class ScourTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target enchantment and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        harness.addToBattlefield(player2, new Compost());
        harness.setHand(player2, List.of(new Compost()));
        harness.setGraveyard(player2, List.of(new Compost()));

        GameData gd = harness.getGameData();
        harness.setLibrary(player2, List.of(new Compost(), new MetathranSoldier()));

        harness.setHand(player1, List.of(new Scour()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Compost");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Compost");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Compost"))
                .hasSize(4);

        harness.assertNotInHand(player2, "Compost");
        harness.assertNotInGraveyard(player2, "Compost");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Compost"));
    }

    @Test
    @DisplayName("Leaves differently-named cards untouched")
    void leavesDifferentlyNamedCardsAlone() {
        harness.addToBattlefield(player2, new Compost());

        GameData gd = harness.getGameData();
        harness.setLibrary(player2, List.of(new MetathranSoldier()));

        harness.setHand(player1, List.of(new Scour()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Compost");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Metathran Soldier"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Metathran Soldier"));
    }

    @Test
    @DisplayName("Fizzles if the target enchantment leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new Compost());
        harness.setHand(player2, List.of(new Compost()));
        harness.setHand(player1, List.of(new Scour()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Compost");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        // No search happens — the copy in hand survives.
        harness.assertInHand(player2, "Compost");
        harness.assertInGraveyard(player1, "Scour");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new MetathranSoldier());
        harness.setHand(player1, List.of(new Scour()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID creatureId = harness.getPermanentId(player2, "Metathran Soldier");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
