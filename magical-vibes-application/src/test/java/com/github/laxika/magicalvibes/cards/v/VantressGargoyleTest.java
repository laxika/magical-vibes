package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();

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

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player2, "Vantress Gargoyle").isBlocking()).isTrue();
    }

    private Permanent addGargoyle(Player player) {
        return addCreatureReady(player, new VantressGargoyle());
    }
}
