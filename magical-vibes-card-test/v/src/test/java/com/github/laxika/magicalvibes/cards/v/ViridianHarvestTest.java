package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.r.RatchetBomb;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViridianHarvest.class, RatchetBomb.class, Shatter.class, Disperse.class})
class ViridianHarvestTest extends BaseCardTest {

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
        harness.castAndResolveInstant(player2, 0, artifact.getId());
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
        harness.castAndResolveInstant(player1, 0, artifact.getId());
        harness.passBothPriorities(); // resolve GainLifeEffect trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

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
        harness.castAndResolveInstant(player2, 0, otherArtifact.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Aura goes to graveyard when enchanted artifact is destroyed")
    void auraGoesToGraveyardWhenArtifactDestroyed() {
        Permanent artifact = addArtifactWithAura(player1, player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities(); // resolve trigger

        // Both artifact and aura should be in graveyards
        harness.assertInGraveyard(player1, "Ratchet Bomb");
        harness.assertInGraveyard(player1, "Viridian Harvest");
        // Neither should be on the battlefield
        harness.assertNotOnBattlefield(player1, "Ratchet Bomb");
        harness.assertNotOnBattlefield(player1, "Viridian Harvest");
    }

    @Test
    @DisplayName("Sacrificing the enchanted artifact triggers life gain before its ability resolves")
    void gainsLifeWhenEnchantedArtifactSacrificed() {
        addArtifactWithAura(player1, player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertLife(player1, lifeBefore);
        harness.assertInGraveyard(player1, "Ratchet Bomb");
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 6);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 6);
        harness.assertInGraveyard(player1, "Viridian Harvest");
    }

    @Test
    @DisplayName("Casting Viridian Harvest attaches it without gaining life until the artifact dies")
    void castAuraAttachesAndTriggersOnDestruction() {
        harness.addToBattlefield(player1, new RatchetBomb());
        Permanent artifact = findPermanent(player1, "Ratchet Bomb");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ViridianHarvest(), new Shatter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Viridian Harvest").getAttachedTo()).isEqualTo(artifact.getId());
        harness.assertLife(player1, lifeBefore);
        harness.castAndResolveInstant(player1, 0, artifact.getId());
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 6);
    }

    @Test
    @DisplayName("Returning the enchanted artifact to hand does not trigger life gain")
    void noLifeWhenEnchantedArtifactReturnedToHand() {
        Permanent artifact = addArtifactWithAura(player1, player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, artifact.getId());

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(artifact.getCard());
        harness.assertInGraveyard(player1, "Viridian Harvest");
        harness.assertNotOnBattlefield(player1, "Ratchet Bomb");
    }

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
