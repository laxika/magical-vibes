package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArtifactSacrificeCostHandlerTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-selects single artifact for sacrifice")
    void autoSelectsSingleArtifact() {
        Card card = createCardWithSacrificeArtifactAbility();
        Permanent source = addReadyPermanent(player1, card);
        addReadyPermanent(player1, createGenericArtifact("Sacrifice Target"));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, idx, null, null);

        // Single artifact should be auto-sacrificed, no prompt
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sacrifice Target"));
    }

    @Test
    @DisplayName("No artifact to sacrifice throws")
    void noArtifactToSacrificeThrows() {
        Card card = createCardWithSacrificeArtifactAbility();
        addReadyPermanent(player1, card);
        // Source is an enchantment, not an artifact — no artifacts to sacrifice

        int idx = gd.playerBattlefields.get(player1.getId()).size() - 1;
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No artifact to sacrifice");
    }

    @Test
    @DisplayName("Multiple artifacts prompt for choice")
    void multipleArtifactsPromptChoice() {
        Card card = createCardWithSacrificeArtifactAbility();
        Permanent source = addReadyPermanent(player1, card);
        addReadyPermanent(player1, createGenericArtifact("Artifact A"));
        addReadyPermanent(player1, createGenericArtifact("Artifact B"));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, idx, null, null);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);
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

    private Card createCardWithSacrificeArtifactAbility() {
        Card card = new Card();
        card.setName("Test Sac Artifact Enchantment");
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                false, null, List.of(new SacrificeArtifactCost(), new PutChargeCounterOnSelfEffect()),
                "Sacrifice an artifact: put a charge counter"
        ));
        return card;
    }
}
