package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseOfTheEldrazi.class, GrizzlyBears.class})
class RiseOfTheEldraziTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a permanent, draws four for the target player, grants an extra turn, and exiles itself")
    void resolvesAllEffects() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RiseOfTheEldrazi()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"), player2.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.assertNotInGraveyard(player1, "Rise of the Eldrazi");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rise of the Eldrazi"));
    }

    @Test
    @DisplayName("Rejects targets in the wrong target groups")
    void enforcesPermanentAndPlayerTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RiseOfTheEldrazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permanent");
    }

    @Test
    @CardUsed(Counterspell.class)
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        RiseOfTheEldrazi rise = new RiseOfTheEldrazi();
        harness.setHand(player1, List.of(rise));
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"), player2.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rise.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Counterspell");
        assertThat(harness.getGameData().extraTurns).containsExactly(player1.getId());
    }
}
