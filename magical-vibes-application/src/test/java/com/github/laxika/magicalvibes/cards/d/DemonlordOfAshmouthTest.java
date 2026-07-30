package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemonlordOfAshmouthTest extends BaseCardTest {

    private void castDemonlord() {
        harness.setHand(player1, List.of(new DemonlordOfAshmouth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    private boolean demonlordExiled() {
        return gd.exiledCards.stream()
                .anyMatch(e -> e.card().getName().equals("Demonlord of Ashmouth"));
    }

    @Test
    @DisplayName("Sacrificing another creature keeps the Demonlord on the battlefield")
    void sacrificeKeepsDemonlord() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castDemonlord();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Demonlord of Ashmouth");
        assertThat(demonlordExiled()).isFalse();
    }

    @Test
    @DisplayName("Declining the sacrifice exiles the Demonlord — undying does not return it")
    void decliningExilesDemonlord() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castDemonlord();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Demonlord of Ashmouth");
        harness.assertNotInGraveyard(player1, "Demonlord of Ashmouth");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(demonlordExiled()).isTrue();
    }

    @Test
    @DisplayName("With no other creature the Demonlord is exiled without a prompt")
    void noOtherCreatureExilesWithoutPrompt() {
        castDemonlord();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Demonlord of Ashmouth");
        assertThat(demonlordExiled()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature can't be sacrificed to save the Demonlord")
    void opponentCreatureDoesNotCount() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castDemonlord();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(demonlordExiled()).isTrue();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
