package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuakefootCyclops.class, GrizzlyBears.class})
class QuakefootCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes up to two target creatures unable to block")
    void enterTheBattlefieldMakesTwoCreaturesCantBlock() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuakefootCyclops()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(bear1.getId(), bear2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bear1.isCantBlockThisTurn()).isTrue();
        assertThat(bear2.isCantBlockThisTurn()).isTrue();
        harness.assertOnBattlefield(player1, "Quakefoot Cyclops");
    }

    @Test
    @DisplayName("ETB may choose no creatures")
    void enterTheBattlefieldMayChooseNoCreatures() {
        harness.setHand(player1, List.of(new QuakefootCyclops()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.<UUID>of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Quakefoot Cyclops");
    }

    @Test
    @DisplayName("Cycling makes a target creature unable to block and draws a card")
    void cyclingMakesTargetCreatureCantBlockAndDraws() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuakefootCyclops()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isCantBlockThisTurn()).isTrue();
        harness.assertInGraveyard(player1, "Quakefoot Cyclops");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
