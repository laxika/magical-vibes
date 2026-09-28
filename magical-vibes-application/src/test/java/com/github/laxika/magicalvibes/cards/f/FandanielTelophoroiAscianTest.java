package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FandanielTelophoroiAscian.class, GrizzlyBears.class, Shock.class})
class FandanielTelophoroiAscianTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils when you cast an instant or sorcery")
    void surveilsOnInstantOrSorceryCast() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent may sacrifice a nontoken creature")
    void opponentMaySacrificeNontokenCreature() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent who declines loses life for each instant or sorcery in your graveyard")
    void opponentDeclinesAndLosesScaledLife() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
