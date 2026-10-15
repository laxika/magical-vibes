package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IngeniousLeonin;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QasaliSlingers.class, IngeniousLeonin.class, GrizzlyBears.class,
        LeoninScimitar.class, RuleOfLaw.class, Conspiracy.class})
class QasaliSlingersTest extends BaseCardTest {

    @Test
    @DisplayName("A Cat entering may destroy a target artifact")
    void catEntryDestroysArtifact() {
        addSlingers();
        harness.addToBattlefield(player2, new LeoninScimitar());

        castCat();
        chooseTarget("Leonin Scimitar");
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("A Cat entering may destroy a target enchantment")
    void catEntryDestroysEnchantment() {
        addSlingers();
        harness.addToBattlefield(player2, new RuleOfLaw());

        castCat();
        chooseTarget("Rule of Law");
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Rule of Law");
    }

    @Test
    @DisplayName("The destruction may be declined")
    void destructionMayBeDeclined() {
        addSlingers();
        harness.addToBattlefield(player2, new LeoninScimitar());

        castCat();
        chooseTarget("Leonin Scimitar");
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("A non-Cat entering does not trigger the ability")
    void nonCatEntryDoesNotTrigger() {
        addSlingers();
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("The trigger cannot target a creature")
    void triggerCannotTargetCreature() {
        addSlingers();
        harness.addToBattlefield(player2, new LeoninScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Qasali Slingers triggers when it enters")
    void selfEntryTriggers() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.castFromHand(player1, new QasaliSlingers(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    void selfEntryTriggersEvenWhenConspiracyMakesSlingersANonCat() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        harness.addToBattlefield(player2, new LeoninScimitar());

        harness.castFromHand(player1, new QasaliSlingers(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        chooseTarget("Leonin Scimitar");
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void opponentCatEntryDoesNotTrigger() {
        addSlingers();
        harness.addToBattlefield(player2, new LeoninScimitar());

        harness.enterBattlefieldAndReturn(player2, new IngeniousLeonin());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    void mayDestroyAnArtifactYouControl() {
        addSlingers();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castCat();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    void catEntryWithNoLegalTargetsLeavesNoInteractionOrAbilityOnStack() {
        addSlingers();

        castCat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ingenious Leonin");
    }

    private void addSlingers() {
        harness.addToBattlefield(player1, new QasaliSlingers());
    }

    private void castCat() {
        harness.castFromHand(player1, new IngeniousLeonin(), "{4}{W}");
        harness.passBothPriorities();
    }

    private void chooseTarget(String cardName) {
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, cardName));
        harness.passBothPriorities();
    }
}
