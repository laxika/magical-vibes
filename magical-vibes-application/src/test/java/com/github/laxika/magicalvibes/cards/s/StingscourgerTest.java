package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stingscourger.class, GrizzlyBears.class})
class StingscourgerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns target creature an opponent controls to its owner's hand")
    void etbReturnsOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolveStingscourger(targetId);

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Stingscourger");
    }

    @Test
    @DisplayName("ETB cannot target a creature its controller controls")
    void etbCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        prepareStingscourger();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Declining echo sacrifices Stingscourger at its next upkeep")
    void decliningEchoSacrificesStingscourger() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveStingscourger(harness.getPermanentId(player2, "Grizzly Bears"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Stingscourger");
        harness.assertInGraveyard(player1, "Stingscourger");
    }

    @Test
    @DisplayName("Paying echo keeps Stingscourger and echo does not trigger again")
    void payingEchoKeepsStingscourgerAndIsOneShot() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveStingscourger(harness.getPermanentId(player2, "Grizzly Bears"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Stingscourger");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Stingscourger");
    }

    private void prepareStingscourger() {
        harness.setHand(player1, List.of(new Stingscourger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castAndResolveStingscourger(UUID targetId) {
        prepareStingscourger();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
