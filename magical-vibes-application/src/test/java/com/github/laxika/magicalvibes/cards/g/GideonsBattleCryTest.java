package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GideonsBattleCry.class, GideonTheOathsworn.class, GrizzlyBears.class, Plains.class})
class GideonsBattleCryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control")
    void putsCountersOnOwnCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GideonTheOathsworn()));

        castBattleCry();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(ownLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Accepting the optional search returns Gideon from the graveyard")
    void searchesGraveyardForGideon() {
        harness.setGraveyard(player1, List.of(new GideonTheOathsworn()));

        castBattleCry();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Gideon, the Oathsworn");
        harness.assertNotInGraveyard(player1, "Gideon, the Oathsworn");
    }

    @Test
    @DisplayName("Accepting the optional search offers Gideon from the library")
    void searchesLibraryForGideon() {
        harness.setLibrary(player1, List.of(new GideonTheOathsworn()));

        castBattleCry();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Gideon, the Oathsworn");
    }

    private void castBattleCry() {
        harness.setHand(player1, List.of(new GideonsBattleCry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
