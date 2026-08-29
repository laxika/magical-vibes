package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalaGedScorpionTest extends BaseCardTest {

    private void castScorpion() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BalaGedScorpion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
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
}
