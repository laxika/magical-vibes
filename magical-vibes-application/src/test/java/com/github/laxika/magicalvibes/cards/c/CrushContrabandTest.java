package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrushContrabandTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode exiles target artifact")
    void artifactModeExilesArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        cast(new int[]{0}, List.of(artifact.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Enchantment mode exiles target enchantment")
    void enchantmentModeExilesEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        cast(new int[]{1}, List.of(enchantment.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Choosing both modes exiles an artifact and an enchantment")
    void bothModesExileBothTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        cast(new int[]{0, 1}, List.of(artifact.getId(), enchantment.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Spellbook", "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment permanent")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CrushContraband()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targets) {
        harness.setHand(player1, List.of(new CrushContraband()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
