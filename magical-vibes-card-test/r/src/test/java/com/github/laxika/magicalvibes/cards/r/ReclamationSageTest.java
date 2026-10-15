package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReclamationSage.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class,
        LeoninScimitar.class, Boomerang.class, DarksteelCitadel.class})
class ReclamationSageTest extends BaseCardTest {

    private void castSage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ReclamationSage(), "{2}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB destroys the chosen artifact")
    void etbDestroysArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castSage();

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Reclamation Sage");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("ETB destroys the chosen enchantment")
    void etbDestroysEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        castSage();

        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Declining the may leaves the artifact alone")
    void decliningLeavesArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castSage();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Reclamation Sage");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Creatures and lands are not offered as targets")
    void nonArtifactNonEnchantmentIsNotAValidTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new LeoninScimitar());
        castSage();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactly(harness.getPermanentId(player2, "Leonin Scimitar"));
    }

    @Test
    @DisplayName("Sage enters without prompting when no legal target exists")
    void noLegalTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        castSage();

        harness.assertOnBattlefield(player1, "Reclamation Sage");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller can destroy their own artifact")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        castSage();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Leonin Scimitar"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Reclamation Sage");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("An indestructible artifact land is a legal target but survives destruction")
    void indestructibleArtifactLandSurvives() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        castSage();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Darksteel Citadel"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target returned to hand before resolution causes the ability to fail")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castSage();

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.handlePermanentChosen(player1, targetId);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Leonin Scimitar");
        harness.assertNotInGraveyard(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Reclamation Sage");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The triggered ability still destroys its target after Sage leaves")
    void sourceLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castSage();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Reclamation Sage"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Reclamation Sage");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.stack).isEmpty();
    }
}
