package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeeperOfTheAccord.class, GrizzlyBears.class, Plains.class, Forest.class})
class KeeperOfTheAccordTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier when the opponent controls more creatures")
    void createsSoldierWhenOpponentControlsMoreCreatures() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveOpponentEndStep(player2);

        assertThat(soldierTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Searches for a basic Plains when the opponent controls more lands")
    void searchesForBasicPlainsWhenOpponentControlsMoreLands() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains()));

        resolveOpponentEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Plains)
                .singleElement()
                .satisfies(land -> assertThat(land.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Does not trigger either ability when the opponent is not ahead")
    void doesNotTriggerWhenOpponentIsNotAhead() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        resolveOpponentEndStep(player2);

        assertThat(soldierTokens(player1)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger during the controller's own end step")
    void doesNotTriggerDuringControllersOwnEndStep() {
        harness.addToBattlefield(player1, new KeeperOfTheAccord());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        resolveOpponentEndStep(player1);

        assertThat(soldierTokens(player1)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveOpponentEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        if (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private long soldierTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Soldier"))
                .count();
    }
}
