package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScarbladeEliteTest extends BaseCardTest {

    private Permanent setup() {
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new ScarbladeElite());
        elite.setSummoningSick(false);
        return elite;
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    @Test
    @DisplayName("Activating prompts to choose an Assassin card to exile")
    void promptsForAssassinExile() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, idxOf(elite), 0, null, bearsId);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Only Assassin cards are valid to exile as the cost")
    void onlyAssassinCardsAreValid() {
        Permanent elite = setup();
        // Graveyard: index 0 non-Assassin (Grizzly Bears), index 1 Assassin (Scarblade Elite)
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new ScarbladeElite()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, idxOf(elite), 0, null, bearsId);

        PendingInteraction.GraveyardExileCostChoice choice =
                (PendingInteraction.GraveyardExileCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Exiles the chosen Assassin and destroys the target creature")
    void exilesAssassinAndDestroysTarget() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, idxOf(elite), 0, null, bearsId);
        harness.handleGraveyardCardChosen(player1, 0);

        // Assassin card exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Scarblade Elite"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scarblade Elite"));

        harness.passBothPriorities();

        // Grizzly Bears destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot activate without an Assassin card in graveyard")
    void cannotActivateWithoutAssassinInGraveyard() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(elite), 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player — the ability only targets creatures")
    void cannotTargetPlayer() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(elite), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
