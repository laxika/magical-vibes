package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KodamaOfTheEastTree.class, GrizzlyBears.class, HillGiant.class})
class KodamaOfTheEastTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Offers a permanent from hand with mana value equal to or less than the entering permanent")
    void offersPermanentWithinEnteringManaValue() {
        harness.addToBattlefield(player1, new KodamaOfTheEastTree());
        harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant()));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("A permanent put in by Kodama does not retrigger that Kodama")
    void permanentPutInByKodamaDoesNotRetriggerIt() {
        harness.addToBattlefield(player1, new KodamaOfTheEastTree());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
    }
}
