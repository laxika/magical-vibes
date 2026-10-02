package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GuardiansOfKoilos;
import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.s.SealAway;
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

@CardUsed({BlessedLight.class, PrimordialWurm.class, SealAway.class, Plains.class,
        GuardiansOfKoilos.class, JoustingLance.class})
class BlessedLightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blessed Light puts it on the stack with target")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm()).getId();
        harness.setHand(player1, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Blessed Light");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving exiles target creature")
    void resolvesAndExilesCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm()).getId();
        harness.setHand(player1, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Primordial Wurm"));
        harness.assertNotInGraveyard(player2, "Primordial Wurm");
    }

    @Test
    @DisplayName("Resolving exiles target enchantment")
    void resolvesAndExilesEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SealAway()).getId();
        harness.setHand(player1, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Seal Away");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Seal Away"));
    }

    @Test
    @DisplayName("Cannot target a non-creature non-enchantment permanent")
    void cannotTargetLand() {
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Plains()).getId();
        harness.setHand(player1, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm()).getId();
        harness.setHand(player1, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can exile a creature controlled by the caster")
    void exilesOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm()).getId();
        harness.setHand(player1, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Primordial Wurm"));
        harness.assertNotInGraveyard(player1, "Primordial Wurm");
        harness.assertInGraveyard(player1, "Blessed Light");
    }

    @Test
    @DisplayName("Can exile an artifact creature")
    void exilesArtifactCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GuardiansOfKoilos()).getId();
        harness.setHand(player1, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Guardians of Koilos");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Guardians of Koilos"));
        harness.assertNotInGraveyard(player2, "Guardians of Koilos");
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new JoustingLance()).getId();
        harness.setHand(player1, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Jousting Lance");
    }
}
