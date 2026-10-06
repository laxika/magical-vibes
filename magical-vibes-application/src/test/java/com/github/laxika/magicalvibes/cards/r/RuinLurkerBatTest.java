package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AnotherChance;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinLurkerBat.class, Forest.class, ZuranOrb.class, AnotherChance.class})
class RuinLurkerBatTest extends BaseCardTest {

    @Test
    @DisplayName("Scries 1 at your end step after a permanent card was put into your graveyard")
    void scriesAfterDescending() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new RuinLurkerBat());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Does not scry at your end step without descending")
    void doesNotScryWithoutDescending() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new RuinLurkerBat());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent descending does not enable your end-step ability")
    void opponentDescendingDoesNotCount() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new RuinLurkerBat());
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Descending does not trigger the Bat during an opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new RuinLurkerBat());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Descending after the end step begins does not trigger the Bat retroactively")
    void descendingDuringEndStepDoesNotTrigger() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new RuinLurkerBat());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("The ability still resolves when the descended permanent card leaves the graveyard")
    void descentDoesNotRequireCardToRemainInGraveyard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new RuinLurkerBat());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Milling a permanent card counts as descending")
    void millingPermanentEnablesScry() {
        harness.addToBattlefield(player1, new RuinLurkerBat());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new AnotherChance()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player1, "Forest");
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Instant cards entering the graveyard do not count as descending")
    void millingNonpermanentsDoesNotEnableScry() {
        harness.addToBattlefield(player1, new RuinLurkerBat());
        harness.setLibrary(player1, List.of(new AnotherChance(), new AnotherChance(), new Forest()));
        harness.setHand(player1, List.of(new AnotherChance()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
