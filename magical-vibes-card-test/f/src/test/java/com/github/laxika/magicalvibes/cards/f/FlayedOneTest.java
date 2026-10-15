package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlayedOne.class, Forest.class})
class FlayedOneTest extends BaseCardTest {

    @Test
    void etbMillsThreeCardsFromControllerLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        harness.setHand(player1, List.of(new FlayedOne()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 3);
    }

    @Test
    void etbMillsOnlyCardsAvailableAndDoesNotMillOpponent() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardBefore = gd.playerGraveyards.get(player2.getId()).size();
        harness.setHand(player1, List.of(new FlayedOne()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardBefore);
    }
    @Test
    void millWaitsForEnterTriggerToResolve() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new FlayedOne()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flayed One");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void enteringWithEmptyLibraryDoesNotCauseLoss() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new FlayedOne()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flayed One");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent flayedOne = addCreatureReady(player1, new FlayedOne());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(flayedOne)));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }
}
