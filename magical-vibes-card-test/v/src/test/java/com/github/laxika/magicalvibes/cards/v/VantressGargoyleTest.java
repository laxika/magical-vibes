package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VantressGargoyle.class, GrizzlyBears.class})
class VantressGargoyleTest extends BaseCardTest {

    @Test
    void activatedAbilityTapsAndMillsBothPlayers() {
        Permanent gargoyle = addGargoyle(player1);
        int player1DeckSize = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gargoyle.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSize - 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSize - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void cannotAttackWithoutSevenCardsInDefendingPlayersGraveyard() {
        addGargoyle(player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAttackWithSevenCardsInDefendingPlayersGraveyard() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        addGargoyle(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void cannotBlockWithFewerThanFourCardsInHand() {
        harness.setHand(player2, List.of());
        addGargoyle(player2);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockWithFourCardsInHand() {
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        addGargoyle(player2);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player2, "Vantress Gargoyle").isBlocking()).isTrue();
    }

    @Test
    void cannotAttackWithSixCardsInDefendingPlayersGraveyardEvenWithSevenInOwn() {
        harness.setGraveyard(player2, List.of(
                new VantressGargoyle(), new VantressGargoyle(), new VantressGargoyle(),
                new VantressGargoyle(), new VantressGargoyle(), new VantressGargoyle()));
        harness.setGraveyard(player1, List.of(
                new VantressGargoyle(), new VantressGargoyle(), new VantressGargoyle(),
                new VantressGargoyle(), new VantressGargoyle(), new VantressGargoyle(),
                new VantressGargoyle()));
        addGargoyle(player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBlockWithThreeCardsInOwnHandEvenWithFourInOpponentsHand() {
        harness.setHand(player2, List.of(
                new VantressGargoyle(), new VantressGargoyle(), new VantressGargoyle()));
        harness.setHand(player1, List.of(
                new VantressGargoyle(), new VantressGargoyle(),
                new VantressGargoyle(), new VantressGargoyle()));
        addGargoyle(player2);
        addCreatureReady(player1, new VantressGargoyle());
        harness.setGraveyard(player2, List.of(
                new VantressGargoyle(), new VantressGargoyle(), new VantressGargoyle(),
                new VantressGargoyle(), new VantressGargoyle(), new VantressGargoyle(),
                new VantressGargoyle()));

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyControllerLibraryDoesNotPreventOpponentFromMilling() {
        harness.setLibrary(player1, List.of());
        VantressGargoyle topCard = new VantressGargoyle();
        VantressGargoyle nextCard = new VantressGargoyle();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        addGargoyle(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
    }

    @Test
    void blockingContinuesAfterControllersHandDropsBelowFourCards() {
        harness.setHand(player2, List.of(
                new VantressGargoyle(), new VantressGargoyle(),
                new VantressGargoyle(), new VantressGargoyle()));
        Permanent gargoyle = addGargoyle(player2);
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        harness.setHand(player2, List.of());
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gargoyle);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    private Permanent addGargoyle(Player player) {
        return addCreatureReady(player, new VantressGargoyle());
    }
}
