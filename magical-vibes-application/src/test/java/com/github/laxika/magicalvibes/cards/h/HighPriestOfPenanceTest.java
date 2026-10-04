package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighPriestOfPenance.class, Shock.class, GrizzlyBears.class, FountainOfYouth.class, Forest.class})
class HighPriestOfPenanceTest extends BaseCardTest {

    private void shockThePriest() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "High Priest of Penance"));
    }

    @Test
    @DisplayName("Damage triggers a may-destroy on the chosen nonland permanent")
    void damageDestroysChosenPermanent() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        shockThePriest();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may leaves the chosen permanent alone")
    void decliningDestroysNothing() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        shockThePriest();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("It can destroy an artifact its own controller controls")
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player1, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");

        shockThePriest();

        harness.handlePermanentChosen(player1, fountainId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("A land is not a legal target")
    void landIsNotALegalTarget() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        shockThePriest();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).doesNotContain(forestId);
    }

    @Test
    @DisplayName("Lethal combat damage still triggers destruction controlled by the priest's controller")
    void lethalCombatDamageStillTriggers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent priest = harness.addToBattlefieldAndReturn(player2, new HighPriestOfPenance());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        priest.setBlocking(true);
        priest.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        harness.assertInGraveyard(player2, "High Priest of Penance");
        harness.handlePermanentChosen(player2, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No destruction ability remains on the stack when only lands survive the damage")
    void noLegalTargetAfterLethalDamage() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player2, new Forest());

        shockThePriest();

        harness.assertInGraveyard(player1, "High Priest of Penance");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Removing the chosen permanent in response prevents destruction without asking for a new target")
    void removedTargetDoesNotRetarget() {
        harness.addToBattlefield(player1, new HighPriestOfPenance());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        shockThePriest();
        harness.handlePermanentChosen(player1, bearsId);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
