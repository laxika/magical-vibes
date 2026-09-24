package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.i.InTheWebOfWar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scour.class, InTheWebOfWar.class, GnarledMass.class})
class ScourTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target enchantment and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        harness.addToBattlefield(player2, new InTheWebOfWar());
        harness.setHand(player2, List.of(new InTheWebOfWar(), new GnarledMass()));
        harness.setGraveyard(player2, List.of(new InTheWebOfWar(), new GnarledMass()));

        harness.setLibrary(player2, List.of(new InTheWebOfWar(), new GnarledMass()));

        harness.setHand(player1, List.of(new Scour()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "In the Web of War");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "In the Web of War");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("In the Web of War"))
                .hasSize(4);

        harness.assertNotInHand(player2, "In the Web of War");
        harness.assertNotInGraveyard(player2, "In the Web of War");
        harness.assertInHand(player2, "Gnarled Mass");
        harness.assertInGraveyard(player2, "Gnarled Mass");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("In the Web of War"))
                .anyMatch(c -> c.getName().equals("Gnarled Mass"));
    }

    @Test
    @DisplayName("Leaves differently-named cards untouched")
    void leavesDifferentlyNamedCardsAlone() {
        harness.addToBattlefield(player2, new InTheWebOfWar());

        harness.setLibrary(player2, List.of(new GnarledMass()));

        harness.setHand(player1, List.of(new Scour()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "In the Web of War");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Gnarled Mass"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Gnarled Mass"));
    }

    @Test
    @DisplayName("Does not exile another same-name permanent on the battlefield")
    void leavesOtherBattlefieldCopyAlone() {
        harness.addToBattlefield(player2, new InTheWebOfWar());
        harness.addToBattlefield(player2, new InTheWebOfWar());

        harness.setHand(player1, List.of(new Scour()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "In the Web of War");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "In the Web of War")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("In the Web of War"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Searches only the target's controller's zones")
    void onlySearchesTargetControllersZones() {
        harness.addToBattlefield(player2, new InTheWebOfWar());
        harness.setHand(player1, List.of(new Scour(), new InTheWebOfWar()));
        harness.setGraveyard(player1, List.of(new InTheWebOfWar()));
        harness.setLibrary(player1, List.of(new InTheWebOfWar()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "In the Web of War");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("In the Web of War"))
                .hasSize(1);
        harness.assertInHand(player1, "In the Web of War");
        harness.assertInGraveyard(player1, "In the Web of War");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("In the Web of War"));
    }

    @Test
    @DisplayName("Fizzles if the target enchantment leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new InTheWebOfWar());
        harness.setHand(player2, List.of(new InTheWebOfWar()));
        harness.setHand(player1, List.of(new Scour()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "In the Web of War");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        // No search happens — the copy in hand survives.
        harness.assertInHand(player2, "In the Web of War");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("In the Web of War"));
        harness.assertInGraveyard(player1, "Scour");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new Scour()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID creatureId = harness.getPermanentId(player2, "Gnarled Mass");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
