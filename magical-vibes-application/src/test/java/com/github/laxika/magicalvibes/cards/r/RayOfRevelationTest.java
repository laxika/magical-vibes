package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RayOfRevelationTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Ray of Revelation destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ray of Revelation"));
    }

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new Telepathy());
        harness.setHand(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Telepathy");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Telepathy"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Telepathy"));
    }

    @Test
    @DisplayName("Cannot target a creature with Ray of Revelation")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback from graveyard destroys target enchantment")
    void flashbackDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ray of Revelation"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ray of Revelation"));
    }

    @Test
    @DisplayName("Flashback spell is exiled when it fizzles")
    void flashbackExilesOnFizzle() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castFlashback(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ray of Revelation"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ray of Revelation"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as instant spell")
    void flashbackPutsOnStackAsSpell() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
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
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castFlashback(player1, 0, targetId);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setGraveyard(player1, List.of(new RayOfRevelation()));

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
