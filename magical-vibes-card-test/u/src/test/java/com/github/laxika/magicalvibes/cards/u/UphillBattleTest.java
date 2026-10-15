package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DoubleMajor;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UphillBattle.class, FreshVolunteers.class, Island.class, DoubleMajor.class})
class UphillBattleTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents' creatures enter tapped")
    void opponentsCreaturesEnterTapped() {
        harness.addToBattlefield(player1, new UphillBattle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");
        harness.passBothPriorities();

        Permanent volunteers = findPermanent(player2, "Fresh Volunteers");
        assertThat(volunteers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures enter untapped")
    void controllersCreaturesEnterUntapped() {
        harness.addToBattlefield(player1, new UphillBattle());

        harness.castFromHand(player1, new FreshVolunteers(), "{1}{W}");
        harness.passBothPriorities();

        Permanent volunteers = findPermanent(player1, "Fresh Volunteers");
        assertThat(volunteers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponents' noncreatures enter untapped")
    void opponentsNoncreaturesEnterUntapped() {
        harness.addToBattlefield(player1, new UphillBattle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Island()));
        harness.playLand(player2, 0);

        Permanent island = findPermanent(player2, "Island");
        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponents' creatures put onto the battlefield enter untapped")
    void opponentsCreaturesPutOntoBattlefieldEnterUntapped() {
        harness.addToBattlefield(player1, new UphillBattle());

        Permanent volunteers = harness.enterBattlefieldAndReturn(player2, new FreshVolunteers());

        assertThat(volunteers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A copied opponent creature spell enters untapped because it was not cast")
    @CardUsed({UphillBattle.class, FreshVolunteers.class, DoubleMajor.class})
    void copiedCreatureSpellEntersUntapped() {
        harness.addToBattlefield(player1, new UphillBattle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        FreshVolunteers volunteers = new FreshVolunteers();
        harness.castFromHand(player2, volunteers, "{1}{W}");
        harness.setHand(player2, List.of(new DoubleMajor()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, volunteers.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> creatures = findPermanents(player2, "Fresh Volunteers");
        assertThat(creatures).hasSize(2);
        assertThat(creatures.stream().filter(p -> p.getCard().isToken()).toList())
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isFalse());
        assertThat(creatures.stream().filter(p -> !p.getCard().isToken()).toList())
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Uphill Battle affects a creature cast before the enchantment entered")
    void enchantmentEnteringBeforeCreatureResolvesMakesItEnterTapped() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FreshVolunteers(), "{1}{W}");

        harness.enterBattlefieldAndReturn(player1, new UphillBattle());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Fresh Volunteers").isTapped()).isTrue();
    }
}
