package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MultiplePermanentSacrificeCostHandlerTest extends BaseCardTest {

    @Test
    @DisplayName("Not enough matching permanents throws")
    void notEnoughMatchingPermanentsThrows() {
        Permanent source = addReadyPermanent(player1, createCardWithMultipleSacrificeCost(3));
        addReadyPermanent(player1, createGenericArtifact("Artifact A"));
        addReadyPermanent(player1, createGenericArtifact("Artifact B"));
        // Only 2 artifacts but need 3

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Auto-selects when exact count matches")
    void autoSelectsWhenExactCountMatches() {
        Permanent source = addReadyPermanent(player1, createCardWithMultipleSacrificeCost(2));
        addReadyPermanent(player1, createGenericArtifact("Artifact A"));
        addReadyPermanent(player1, createGenericArtifact("Artifact B"));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, idx, null, null);

        // Exactly 2 artifacts for 2 required — should auto-sacrifice both
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Artifact A"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Artifact B"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("More than required prompts for choice")
    void moreThanRequiredPromptsForChoice() {
        Permanent source = addReadyPermanent(player1, createCardWithMultipleSacrificeCost(2));
        addReadyPermanent(player1, createGenericArtifact("Artifact A"));
        addReadyPermanent(player1, createGenericArtifact("Artifact B"));
        addReadyPermanent(player1, createGenericArtifact("Artifact C"));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, idx, null, null);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Non-matching permanents are not counted")
    void nonMatchingPermanentsNotCounted() {
        Permanent source = addReadyPermanent(player1, createCardWithMultipleSacrificeCost(2));
        addReadyPermanent(player1, createGenericArtifact("Artifact A"));
        addReadyPermanent(player1, createNonArtifactPermanent("Enchantment A"));
        // Only 1 artifact but need 2

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createGenericArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setColor(null);
        return card;
    }

    private Card createNonArtifactPermanent(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{0}");
        card.setColor(null);
        return card;
    }

    private Card createCardWithMultipleSacrificeCost(int count) {
        Card card = new Card();
        card.setName("Test Multiple Sacrifice Source");
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new SacrificeMultiplePermanentsCost(count, new PermanentIsArtifactPredicate()),
                        new PutChargeCounterOnSelfEffect()),
                "Sacrifice " + count + " artifacts: put a charge counter"
        ));
        return card;
    }
}
