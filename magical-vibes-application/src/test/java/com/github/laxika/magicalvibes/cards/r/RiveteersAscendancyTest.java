package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighMarket;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiveteersAscendancy.class, HighMarket.class, HillGiant.class, GrizzlyBears.class})
class RiveteersAscendancyTest extends BaseCardTest {

    @Test
    void returnsTappedCreatureWithLowerManaValue() {
        addAscendancyAndMarket();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Card lowerManaValueCreature = new GrizzlyBears();
        Card equalManaValueCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(lowerManaValueCreature, equalManaValueCreature));

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == lowerManaValueCreature)
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(sacrificed.getCard(), equalManaValueCreature);
    }

    @Test
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new RiveteersAscendancy());
        harness.addToBattlefield(player1, new HighMarket());
        harness.addToBattlefield(player1, new HighMarket());
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Card returnedCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCreature));

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player1, 2, 1, null, null);
        harness.handlePermanentChosen(player1, secondSacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == returnedCreature))
                .hasSize(1);
    }

    private void addAscendancyAndMarket() {
        harness.addToBattlefield(player1, new RiveteersAscendancy());
        harness.addToBattlefield(player1, new HighMarket());
    }
}
