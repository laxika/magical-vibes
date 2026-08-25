package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Glory.class, GrizzlyBears.class})
class GloryTest extends BaseCardTest {

    @Test
    @DisplayName("The ability can only be activated while Glory is in a graveyard")
    void abilityIsOnlyAvailableFromGraveyard() {
        addCreatureReady(player1, new Glory());
        prepareAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Grants your creatures protection from the chosen color")
    void grantsOwnCreaturesProtectionFromChosenColor() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Glory()));
        prepareAbilityMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opposingCreature, CardColor.RED)).isFalse();
        harness.assertInGraveyard(player1, "Glory");
    }

    @Test
    @DisplayName("Protection granted by Glory expires at end of turn")
    void protectionExpiresAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Glory()));
        prepareAbilityMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLUE)).isFalse();
    }

    private void prepareAbilityMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
