package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiseOfTheDarkRealmsTest extends BaseCardTest {

    private void castRiseOfTheDarkRealms() {
        harness.setHand(player1, new ArrayList<>(List.of(new RiseOfTheDarkRealms())));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns creatures from the controller's graveyard to the battlefield")
    void returnsFromControllerGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(creature);

        castRiseOfTheDarkRealms();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Returns creatures from an opponent's graveyard under the caster's control")
    void returnsOpponentCreaturesUnderCasterControl() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentCreature = new GrizzlyBears();
        gd.playerGraveyards.get(player2.getId()).add(opponentCreature);

        castRiseOfTheDarkRealms();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns creatures from both graveyards at once, all under the caster's control")
    void returnsFromBothGraveyards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new RagingGoblin());

        castRiseOfTheDarkRealms();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Raging Goblin");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Leaves non-creature cards in the graveyards")
    void leavesNonCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card enchantment = new GloriousAnthem();
        Card land = new Mountain();
        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(enchantment, land, creature));

        castRiseOfTheDarkRealms();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enchantment, land)
                .anyMatch(c -> c.getName().equals("Rise of the Dark Realms"))
                .hasSize(3);
    }

    @Test
    @DisplayName("Resolves harmlessly with empty graveyards")
    void worksWithEmptyGraveyards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castRiseOfTheDarkRealms();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Rise of the Dark Realms"));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
