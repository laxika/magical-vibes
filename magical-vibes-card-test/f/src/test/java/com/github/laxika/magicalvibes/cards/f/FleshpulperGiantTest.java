package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
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

@CardUsed({FleshpulperGiant.class, DeadlyRecluse.class, CanyonMinotaur.class, GiantGrowth.class, Disperse.class})
class FleshpulperGiantTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FleshpulperGiant(), "{5}{R}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the ETB may destroys a creature with toughness 2 or less")
    void destroysSmallCreature() {
        harness.addToBattlefield(player2, new DeadlyRecluse());
        UUID recluseId = harness.getPermanentId(player2, "Deadly Recluse");

        cast();
        harness.handlePermanentChosen(player1, recluseId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Deadly Recluse");
        harness.assertInGraveyard(player2, "Deadly Recluse");
        harness.assertOnBattlefield(player1, "Fleshpulper Giant");
    }

    @Test
    @DisplayName("Declining the may leaves the creature alive")
    void decliningLeavesCreatureAlive() {
        harness.addToBattlefield(player2, new DeadlyRecluse());
        UUID recluseId = harness.getPermanentId(player2, "Deadly Recluse");

        cast();
        harness.handlePermanentChosen(player1, recluseId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Deadly Recluse");
        harness.assertOnBattlefield(player1, "Fleshpulper Giant");
    }

    @Test
    @DisplayName("No trigger when only creatures with toughness 3 or more are present")
    void noTriggerWithoutLegalTarget() {
        harness.addToBattlefield(player2, new CanyonMinotaur());
        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Canyon Minotaur");
        harness.assertOnBattlefield(player1, "Fleshpulper Giant");
    }

    @Test
    @DisplayName("Only the toughness 2 or less creature may be chosen")
    void onlySmallCreatureIsLegalTarget() {
        harness.addToBattlefield(player2, new DeadlyRecluse());
        harness.addToBattlefield(player2, new CanyonMinotaur());
        UUID minotaurId = harness.getPermanentId(player2, "Canyon Minotaur");

        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).doesNotContain(minotaurId);

        assertThat(choice.validPermanentIds()).contains(harness.getPermanentId(player2, "Deadly Recluse"));
    }

    @Test
    @DisplayName("Can target the controller's own small creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new DeadlyRecluse());
        UUID recluseId = harness.getPermanentId(player1, "Deadly Recluse");

        cast();
        harness.handlePermanentChosen(player1, recluseId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Deadly Recluse");
    }

    @Test
    @DisplayName("The ability does not resolve if the target grows above toughness two")
    void targetGrowingBeforeResolutionSurvives() {
        harness.addToBattlefield(player2, new DeadlyRecluse());
        UUID recluseId = harness.getPermanentId(player2, "Deadly Recluse");
        cast();
        harness.handlePermanentChosen(player1, recluseId);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, recluseId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Deadly Recluse");
    }

    @Test
    @DisplayName("The ability does not resolve if its target leaves the battlefield")
    void targetReturnedToHandBeforeResolution() {
        harness.addToBattlefield(player2, new DeadlyRecluse());
        UUID recluseId = harness.getPermanentId(player2, "Deadly Recluse");
        cast();
        harness.handlePermanentChosen(player1, recluseId);

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, recluseId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Deadly Recluse");
        harness.assertNotOnBattlefield(player2, "Deadly Recluse");
    }
}
