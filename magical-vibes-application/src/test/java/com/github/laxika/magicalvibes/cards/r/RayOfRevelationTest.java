package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TestOfEndurance;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({RayOfRevelation.class, TestOfEndurance.class, SuntailHawk.class})
class RayOfRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ray of Revelation destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new TestOfEndurance());
        harness.setHand(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Test of Endurance");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Test of Endurance");
        harness.assertInGraveyard(player2, "Test of Endurance");
        harness.assertInGraveyard(player1, "Ray of Revelation");
    }

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new TestOfEndurance());
        harness.setHand(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Test of Endurance");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Test of Endurance");
        harness.assertInGraveyard(player1, "Test of Endurance");
    }

    @Test
    @DisplayName("Cannot target a creature with Ray of Revelation")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Suntail Hawk");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback from graveyard destroys target enchantment")
    void flashbackDestroysEnchantment() {
        harness.addToBattlefield(player2, new TestOfEndurance());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Test of Endurance");
        harness.castAndResolveFlashback(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Test of Endurance");
        harness.assertInGraveyard(player2, "Test of Endurance");
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player2, new TestOfEndurance());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Test of Endurance");
        harness.castAndResolveFlashback(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Ray of Revelation");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ray of Revelation"));
    }

    @Test
    @DisplayName("Flashback spell is exiled when it fizzles")
    void flashbackExilesOnFizzle() {
        harness.addToBattlefield(player2, new TestOfEndurance());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Test of Endurance");
        harness.castFlashback(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertNotInGraveyard(player1, "Ray of Revelation");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ray of Revelation"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as instant spell")
    void flashbackPutsOnStackAsSpell() {
        harness.addToBattlefield(player2, new TestOfEndurance());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Test of Endurance");
        harness.castFlashback(player1, 0, targetId);

        StackEntry entry = gd.stack.getFirst();
        assertThat(gd.stack).hasSize(1);
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Ray of Revelation");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Flashback pays the flashback cost, not the mana cost")
    void flashbackPaysFlashbackCost() {
        harness.addToBattlefield(player2, new TestOfEndurance());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Test of Endurance");
        harness.castFlashback(player1, 0, targetId);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.addToBattlefield(player2, new TestOfEndurance());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));

        UUID targetId = harness.getPermanentId(player2, "Test of Endurance");
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
