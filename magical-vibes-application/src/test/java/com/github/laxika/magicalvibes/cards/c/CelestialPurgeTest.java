package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.UnderworldDreams;
import com.github.laxika.magicalvibes.cards.w.WarpathGhoul;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelestialPurge.class, WarpathGhoul.class, CanyonMinotaur.class, RuneclawBear.class,
        UnderworldDreams.class, BlackKnight.class})
class CelestialPurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Celestial Purge puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new WarpathGhoul());
        harness.setHand(player1, List.of(new CelestialPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Warpath Ghoul");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Celestial Purge");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving exiles target black permanent")
    void resolvesAndExilesBlackPermanent() {
        harness.addToBattlefield(player2, new WarpathGhoul());
        harness.setHand(player1, List.of(new CelestialPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Warpath Ghoul");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Warpath Ghoul");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Warpath Ghoul"));
        harness.assertNotInGraveyard(player2, "Warpath Ghoul");
    }

    @Test
    @DisplayName("Resolving exiles target red permanent")
    void resolvesAndExilesRedPermanent() {
        harness.addToBattlefield(player2, new CanyonMinotaur());
        harness.setHand(player1, List.of(new CelestialPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Canyon Minotaur");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Canyon Minotaur");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Canyon Minotaur"));
        harness.assertNotInGraveyard(player2, "Canyon Minotaur");
    }

    @Test
    @DisplayName("Cannot target a non-black non-red permanent")
    void cannotTargetNonBlackNonRedPermanent() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new CelestialPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Runeclaw Bear");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new WarpathGhoul());
        harness.setHand(player1, List.of(new CelestialPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Warpath Ghoul");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles a black noncreature permanent")
    void exilesBlackEnchantment() {
        harness.addToBattlefield(player2, new UnderworldDreams());
        harness.setHand(player1, List.of(new CelestialPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Underworld Dreams"));

        harness.assertNotOnBattlefield(player2, "Underworld Dreams");
        harness.assertNotInGraveyard(player2, "Underworld Dreams");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Underworld Dreams"));
        harness.assertInGraveyard(player1, "Celestial Purge");
    }

    @Test
    @DisplayName("Can exile your own red permanent")
    void exilesOwnRedPermanent() {
        harness.addToBattlefield(player1, new CanyonMinotaur());
        harness.setHand(player1, List.of(new CelestialPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Canyon Minotaur"));

        harness.assertNotOnBattlefield(player1, "Canyon Minotaur");
        harness.assertNotInGraveyard(player1, "Canyon Minotaur");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Canyon Minotaur"));
    }

    @Test
    @DisplayName("Cannot target a black creature with protection from white")
    void cannotTargetProtectionFromWhite() {
        harness.addToBattlefield(player2, new BlackKnight());
        harness.setHand(player1, List.of(new CelestialPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Black Knight");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Black Knight");
    }
}
