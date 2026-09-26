package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CentaurVinecrasher.class, Forest.class, GrizzlyBears.class})
class CentaurVinecrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each land card in all graveyards")
    void entersWithCountersForAllGraveyardLands() {
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Forest(), new Forest()));

        Permanent vinecrasher = harness.enterBattlefieldAndReturn(player1, new CentaurVinecrasher());

        assertThat(vinecrasher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("May pay {G}{G} to return it after any land enters a graveyard")
    void returnsToHandWhenAnyLandEntersAGraveyard() {
        CentaurVinecrasher vinecrasher = new CentaurVinecrasher();
        harness.setGraveyard(player1, List.of(vinecrasher));
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(vinecrasher);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(vinecrasher);
    }

    @Test
    @DisplayName("A nonland card entering a graveyard does not trigger it")
    void nonlandDoesNotTrigger() {
        CentaurVinecrasher vinecrasher = new CentaurVinecrasher();
        harness.setGraveyard(player1, List.of(vinecrasher));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vinecrasher);
    }
}
