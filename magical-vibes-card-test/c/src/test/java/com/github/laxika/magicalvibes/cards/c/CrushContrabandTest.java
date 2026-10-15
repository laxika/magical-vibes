package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BidentOfThassa;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrushContraband.class, Spellbook.class, GloriousAnthem.class, GrizzlyBears.class, BidentOfThassa.class})
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

    @Test
    @DisplayName("Both modes can target the same artifact enchantment")
    void bothModesCanShareTarget() {
        Permanent bident = harness.addToBattlefieldAndReturn(player2, new BidentOfThassa());
        cast(new int[]{0, 1}, List.of(bident.getId(), bident.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bident of Thassa");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Bident of Thassa");
        harness.assertInGraveyard(player1, "Crush Contraband");
    }

    @Test
    @DisplayName("The enchantment is still exiled if the artifact leaves before resolution")
    void resolvesWithOnlyEnchantmentTargetRemaining() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        cast(new int[]{0, 1}, List.of(artifact.getId(), enchantment.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Glorious Anthem");
    }

    @Test
    @DisplayName("The artifact is still exiled if the enchantment leaves before resolution")
    void resolvesWithOnlyArtifactTargetRemaining() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        cast(new int[]{0, 1}, List.of(artifact.getId(), enchantment.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchantment);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Spellbook");
    }

    @Test
    @DisplayName("The spell does not resolve when both targets leave the battlefield")
    void allTargetsIllegal() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        cast(new int[]{0, 1}, List.of(artifact.getId(), enchantment.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchantment);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Crush Contraband");
        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Artifact and enchantment targets must match their respective modes")
    void cannotSwapTargetsBetweenModes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        assertThatThrownBy(() -> cast(new int[]{0, 1}, List.of(enchantment.getId(), artifact.getId())))
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
