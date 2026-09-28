package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.w.WarpedDevotion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WreckAndRebuild.class, Forest.class, GrizzlyBears.class, ManaCylix.class, WarpedDevotion.class})
class WreckAndRebuildTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact or enchantment")
    void destroysArtifactOrEnchantment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
        castDestructionMode(artifact.getId());

        harness.assertNotOnBattlefield(player2, "Mana Cylix");
    }

    @Test
    void destroysAnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new WarpedDevotion());
        castDestructionMode(enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Warped Devotion");
    }

    @Test
    void rejectsANonArtifactOrEnchantmentTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("Rebuild mills five cards and may return a land tapped")
    void rebuildMillsAndReturnsLandTapped() {
        Forest returnedLand = new Forest();
        harness.setLibrary(player1, List.of(
                returnedLand, new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addMana();

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        int graveyardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(returnedLand);
        harness.handleGraveyardCardChosen(player1, graveyardIndex);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(returnedLand.getId()))
                .singleElement()
                .matches(Permanent::isTapped);
    }

    private void castDestructionMode(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addMana();
        harness.castModalSorcery(player1, 0, 0, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
