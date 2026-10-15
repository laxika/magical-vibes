package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.m.Mob;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuakefootCyclops.class, Mob.class})
class QuakefootCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes up to two target creatures unable to block")
    void enterTheBattlefieldMakesTwoCreaturesCantBlock() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new QuakefootCyclops());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new QuakefootCyclops());
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
        harness.castFromHand(player1, new QuakefootCyclops(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Quakefoot Cyclops");
    }

    @Test
    @DisplayName("Cycling makes a target creature unable to block and draws a card")
    void cyclingMakesTargetCreatureCantBlockAndDraws() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new QuakefootCyclops());
        harness.setHand(player1, List.of(new QuakefootCyclops()));
        harness.setLibrary(player1, List.of(new QuakefootCyclops()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isCantBlockThisTurn()).isTrue();
        harness.assertInGraveyard(player1, "Quakefoot Cyclops");
        harness.assertNotInHand(player1, "Quakefoot Cyclops");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Quakefoot Cyclops");
    }

    @Test
    @DisplayName("Cycling without any creatures still draws a card")
    void cyclingWithoutCreaturesStillDraws() {
        harness.setHand(player1, List.of(new QuakefootCyclops()));
        harness.setLibrary(player1, List.of(new QuakefootCyclops()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Quakefoot Cyclops");
        harness.assertNotInHand(player1, "Quakefoot Cyclops");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Quakefoot Cyclops");
    }

    @Test
    @DisplayName("Removing the cycling trigger target does not prevent drawing")
    void cyclingDrawsWhenTargetIsDestroyed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QuakefootCyclops());
        harness.setHand(player1, List.of(new QuakefootCyclops()));
        harness.setLibrary(player1, List.of(new QuakefootCyclops()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Mob()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertNotOnBattlefield(player2, "Quakefoot Cyclops");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quakefoot Cyclops");
        harness.assertInHand(player1, "Quakefoot Cyclops");
    }

    @Test
    @DisplayName("ETB can choose exactly one creature controlled by its controller")
    void enterTheBattlefieldCanTargetOneOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QuakefootCyclops());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new QuakefootCyclops());
        harness.setHand(player1, List.of(new QuakefootCyclops()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(unchosen.isCantBlockThisTurn()).isFalse();
    }
}
