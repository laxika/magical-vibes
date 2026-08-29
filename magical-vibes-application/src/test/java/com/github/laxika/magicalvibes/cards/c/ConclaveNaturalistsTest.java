package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConclaveNaturalistsTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ConclaveNaturalists()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void castAndAcceptMay(UUID targetId) {
        cast();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("ETB destroys the chosen target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castAndAcceptMay(harness.getPermanentId(player2, "Leonin Scimitar"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Conclave Naturalists");
    }

    @Test
    @DisplayName("ETB destroys the chosen target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new RuleOfLaw());
        castAndAcceptMay(harness.getPermanentId(player2, "Rule of Law"));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rule of Law");
    }

    @Test
    @DisplayName("Declining the may ability leaves the permanent alone")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new RuleOfLaw());
        cast();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Rule of Law"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Rule of Law");
        harness.assertOnBattlefield(player1, "Conclave Naturalists");
    }

    @Test
    @DisplayName("No may prompt when only creatures are on the battlefield")
    void noMayPromptWithoutLegalTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConclaveNaturalists()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Conclave Naturalists");
    }

    @Test
    @DisplayName("Chooses the trigger target before the may decision")
    void choosesTargetBeforeMayDecision() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
