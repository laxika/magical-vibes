package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoleManMoloidMaster.class, Forest.class, GrizzlyBears.class})
class MoleManMoloidMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Can play a land from the controller's graveyard")
    void canPlayLandFromGraveyard() {
        harness.addToBattlefield(player1, new MoleManMoloidMaster());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Landfall creates a 1/1 green Moloid Minion token")
    void landfallCreatesMoloid() {
        harness.addToBattlefield(player1, new MoleManMoloidMaster());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent moloid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Moloid"))
                .findFirst()
                .orElseThrow();
        assertThat(moloid.getEffectivePower()).isEqualTo(1);
        assertThat(moloid.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with a Moloid may mill a card")
    void moloidMayMillWhenAttacking() {
        harness.addToBattlefield(player1, new MoleManMoloidMaster());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent moloid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Moloid"))
                .findFirst()
                .orElseThrow();
        moloid.setSummoningSick(false);
        GrizzlyBears milled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milled));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(moloid)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
    }

    @Test
    @DisplayName("The land-play permission is limited to the controller")
    void onlyControllerCanPlayLandFromGraveyard() {
        harness.addToBattlefield(player1, new MoleManMoloidMaster());
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player2, List.of());
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.playGraveyardLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
