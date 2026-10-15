package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConclaveNaturalists.class, GrizzlyBears.class, LeoninScimitar.class, RuleOfLaw.class})
class ConclaveNaturalistsTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ConclaveNaturalists(), "{4}{G}");
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
        cast();

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

    @Test
    @DisplayName("ETB can destroy an artifact controlled by its controller")
    void etbCanDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        castAndAcceptMay(harness.getPermanentId(player1, "Leonin Scimitar"));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Conclave Naturalists");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB rejects a creature that is neither an artifact nor an enchantment")
    void etbRejectsCreatureOnlyTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LeoninScimitar());
        cast();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.stack).isEmpty();
    }
}
