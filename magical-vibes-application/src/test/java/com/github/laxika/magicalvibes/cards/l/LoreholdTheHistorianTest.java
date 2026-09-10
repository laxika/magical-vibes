package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoreholdTheHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("The first drawn instant is offered for miracle {2}")
    void grantsMiracleToDrawnInstant() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setLibrary(player1, List.of(new Opt()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.manaCost()).isEqualTo("{2}");
    }

    @Test
    @DisplayName("The granted miracle cost remains available after Lorehold leaves the battlefield")
    void snapshotsGrantedMiracleCost() {
        Permanent lorehold = harness.addToBattlefieldAndReturn(player1, new LoreholdTheHistorian());
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).remove(lorehold);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.manaCost()).isEqualTo("{2}");
    }

    @Test
    @DisplayName("A drawn creature is not offered the granted miracle")
    void doesNotGrantMiracleToCreatures() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent upkeep offers a discard followed by a draw")
    void opponentUpkeepOffersDiscardAndDraw() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
