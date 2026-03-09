package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RatchetBomb;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViridianHarvestTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Viridian Harvest has enchanted permanent death trigger with GainLifeEffect(6)")
    void hasEnchantedPermanentDeathTrigger() {
        ViridianHarvest card = new ViridianHarvest();

        assertThat(card.isAura()).isTrue();
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD).getFirst())
                .isInstanceOf(GainLifeEffect.class);
        GainLifeEffect gainLife = (GainLifeEffect) card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD).getFirst();
        assertThat(gainLife.amount()).isEqualTo(6);
    }

    // ===== Trigger fires when enchanted artifact is destroyed =====

    @Test
    @DisplayName("Controller gains 6 life when enchanted artifact is destroyed")
    void gainsLifeWhenEnchantedArtifactDestroyed() {
        Permanent artifact = addArtifactWithAura(player1, player1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Opponent destroys the enchanted artifact with Shatter
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities(); // resolve Shatter — artifact destroyed, trigger goes on stack
        harness.passBothPriorities(); // resolve GainLifeEffect trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    @DisplayName("Aura controller gains life even when enchanting opponent's artifact")
    void auraControllerGainsLifeWhenEnchantingOpponentArtifact() {
        // Player 1 controls the aura, Player 2 controls the artifact
        Permanent artifact = addArtifactWithAura(player2, player1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Player 1 destroys the artifact with Shatter
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities(); // resolve Shatter
        harness.passBothPriorities(); // resolve GainLifeEffect trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    // ===== No trigger when different artifact is destroyed =====

    @Test
    @DisplayName("No life gained when a different artifact is destroyed")
    void noLifeWhenDifferentArtifactDestroyed() {
        Permanent enchantedArtifact = addArtifactWithAura(player1, player1);
        // Add a second artifact (not enchanted)
        harness.addToBattlefield(player1, new RatchetBomb());
        Permanent otherArtifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId() != enchantedArtifact.getId() && !p.getCard().getName().equals("Viridian Harvest"))
                .findFirst().orElseThrow();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Opponent destroys the non-enchanted artifact
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, otherArtifact.getId());
        harness.passBothPriorities(); // resolve Shatter

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Aura goes to graveyard when enchanted artifact is destroyed =====

    @Test
    @DisplayName("Aura goes to graveyard when enchanted artifact is destroyed")
    void auraGoesToGraveyardWhenArtifactDestroyed() {
        Permanent artifact = addArtifactWithAura(player1, player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities(); // resolve Shatter
        harness.passBothPriorities(); // resolve trigger

        // Both artifact and aura should be in graveyards
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ratchet Bomb"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Viridian Harvest"));
        // Neither should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ratchet Bomb"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Viridian Harvest"));
    }

    // ===== Helpers =====

    /**
     * Places a Ratchet Bomb on the artifact controller's battlefield and attaches
     * a Viridian Harvest controlled by the aura controller.
     *
     * @return the Ratchet Bomb permanent
     */
    private Permanent addArtifactWithAura(Player artifactController, Player auraController) {
        harness.addToBattlefield(artifactController, new RatchetBomb());
        Permanent artifact = gd.playerBattlefields.get(artifactController.getId()).getFirst();

        ViridianHarvest auraCard = new ViridianHarvest();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);

        return artifact;
    }
}
