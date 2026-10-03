package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfTheMasses;
import com.github.laxika.magicalvibes.cards.o.OvergrownBattlement;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalaGedScorpion.class, FugitiveWizard.class, GrizzlyBears.class,
        OvergrownBattlement.class, MightOfTheMasses.class})
class BalaGedScorpionTest extends BaseCardTest {

    private void castScorpion() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new BalaGedScorpion(), "{3}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB may destroys a creature with power 1 or less")
    void etbMayDestroysSmallCreature() {
        UUID wizardId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();

        castScorpion();
        harness.handlePermanentChosen(player1, wizardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Bala Ged Scorpion");
    }

    @Test
    @DisplayName("Declining the ETB may leaves the small creature alive")
    void decliningMayLeavesSmallCreatureAlive() {
        UUID wizardId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();

        castScorpion();
        harness.handlePermanentChosen(player1, wizardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Bala Ged Scorpion");
    }

    @Test
    @DisplayName("ETB target selection excludes creatures with power greater than 1")
    void targetSelectionOnlyIncludesSmallCreatures() {
        UUID wizardId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        castScorpion();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(wizardId).doesNotContain(bearsId);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No ETB may prompt occurs when no creature has power 1 or less")
    void noPromptWithoutSmallCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castScorpion();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Bala Ged Scorpion");
    }

    @Test
    @DisplayName("ETB can destroy a zero-power creature controlled by its controller")
    void destroysOwnZeroPowerCreature() {
        UUID wallId = harness.addToBattlefieldAndReturn(player1, new OvergrownBattlement()).getId();

        castScorpion();
        harness.handlePermanentChosen(player1, wallId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Overgrown Battlement");
        harness.assertInGraveyard(player1, "Overgrown Battlement");
        harness.assertOnBattlefield(player1, "Bala Ged Scorpion");
    }

    @Test
    @DisplayName("ETB does not resolve when the target's power increases above one in response")
    void targetPowerIsRecheckedOnResolution() {
        UUID wizardId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();

        castScorpion();
        harness.handlePermanentChosen(player1, wizardId);
        harness.setHand(player2, List.of(new MightOfTheMasses()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, wizardId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertNotInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("ETB uses current power rather than printed power when choosing a target")
    void boostedCreatureCannotBeChosen() {
        UUID wizardId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();
        harness.setHand(player2, List.of(new MightOfTheMasses()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, wizardId);

        castScorpion();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Bala Ged Scorpion");
    }
}
