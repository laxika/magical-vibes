package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SeaGateColossus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NimanaSkydancer.class, SeaGateColossus.class})
class NimanaSkydancerTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldMillsTwoCardsFromTargetOpponent() {
        harness.setLibrary(player2, List.of(new SeaGateColossus(), new SeaGateColossus(), new SeaGateColossus()));
        harness.setHand(player1, List.of(new NimanaSkydancer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new NimanaSkydancer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void millsOnlyTheTopTwoCardsAndLeavesTheControllersLibraryAlone() {
        SeaGateColossus top = new SeaGateColossus();
        NimanaSkydancer second = new NimanaSkydancer();
        SeaGateColossus bottom = new SeaGateColossus();
        SeaGateColossus ownCard = new SeaGateColossus();
        harness.setLibrary(player2, List.of(top, second, bottom));
        harness.setLibrary(player1, List.of(ownCard));
        harness.setHand(player1, List.of(new NimanaSkydancer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, second, bottom);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bottom);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(top, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void millsTheOnlyCardInAShortLibrary() {
        SeaGateColossus onlyCard = new SeaGateColossus();
        harness.setLibrary(player2, List.of(onlyCard));
        harness.setHand(player1, List.of(new NimanaSkydancer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(onlyCard);
        harness.assertOnBattlefield(player1, "Nimana Skydancer");
    }

    @Test
    void resolvesAgainstAnEmptyLibrary() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new NimanaSkydancer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Nimana Skydancer");
    }

    @Test
    void canBeCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setLibrary(player2, List.of(new SeaGateColossus(), new SeaGateColossus()));
        harness.setHand(player1, List.of(new NimanaSkydancer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nimana Skydancer");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
