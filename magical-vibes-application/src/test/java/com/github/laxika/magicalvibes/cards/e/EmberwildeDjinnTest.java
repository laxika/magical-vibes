package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberwildeDjinn.class})
class EmberwildeDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent pays {R}{R} during their upkeep and gains control of the Djinn")
    void opponentPaysManaAndStealsDjinn() {
        UUID djinnId = harness.addToBattlefieldAndReturn(player1, new EmberwildeDjinn()).getId();

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(controls(player2, djinnId)).isTrue();
        assertThat(controls(player1, djinnId)).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("With no red mana, the opponent pays 2 life instead to gain control")
    void opponentPaysLifeAndStealsDjinn() {
        UUID djinnId = harness.addToBattlefieldAndReturn(player1, new EmberwildeDjinn()).getId();

        advanceToUpkeep(player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(controls(player2, djinnId)).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Declining the payment leaves the Djinn with its controller")
    void decliningLeavesControlUnchanged() {
        UUID djinnId = harness.addToBattlefieldAndReturn(player1, new EmberwildeDjinn()).getId();

        advanceToUpkeep(player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(controls(player1, djinnId)).isTrue();
        assertThat(controls(player2, djinnId)).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The controller is also asked during their own upkeep and simply keeps the Djinn")
    void controllerIsAskedDuringOwnUpkeep() {
        UUID djinnId = harness.addToBattlefieldAndReturn(player1, new EmberwildeDjinn()).getId();

        advanceToUpkeep(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controls(player1, djinnId)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("A player who can pay neither mana nor life is never prompted")
    void noPromptWhenNeitherResourceIsAvailable() {
        UUID djinnId = harness.addToBattlefieldAndReturn(player1, new EmberwildeDjinn()).getId();

        advanceToUpkeep(player2);
        gd.playerLifeTotals.put(player2.getId(), 1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(controls(player1, djinnId)).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A returned Djinn is not affected by an upkeep trigger from its previous existence")
    void returnedDjinnIsNotAffectedByPreviousTrigger() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new EmberwildeDjinn());

        advanceToUpkeep(player2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, original));
        gd.playerGraveyards.get(player1.getId()).remove(original.getCard());
        Permanent returned = harness.enterBattlefieldAndReturn(player1, original.getCard());

        harness.addMana(player2, ManaColor.RED, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(controls(player1, returned.getId())).isTrue();
        assertThat(controls(player2, returned.getId())).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    private boolean controls(Player player, UUID permanentId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getId().equals(permanentId));
    }
}
