package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AsForetold;
import com.github.laxika.magicalvibes.cards.e.Electrodominance;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RampagingRendhorn;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaviniaAzoriusRenegade.class, AsForetold.class, GrizzlyBears.class, Plains.class,
        WrathOfGod.class, Electrodominance.class, RampagingRendhorn.class})
class LaviniaAzoriusRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent can't cast a noncreature spell with mana value greater than their land count")
    void restrictsNoncreatureSpellByOpponentsLandCount() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent can cast a noncreature spell at or below their land count")
    void allowsNoncreatureSpellAtLandCount() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Plains());
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Counters an opponent's spell cast without spending mana")
    void countersOpponentFreeCast() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        var asForetold = harness.addToBattlefieldAndReturn(player2, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not counter an opponent's spell cast with mana")
    void doesNotCounterPaidSpell() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void controllerCanCastNoncreatureSpellWithNoLands() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wrath of God");
        harness.assertInGraveyard(player1, "Lavinia, Azorius Renegade");
    }

    @Test
    void controllerFreeSpellIsNotCountered() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        var asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentCanCastExpensiveCreatureWithNoLands() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new RampagingRendhorn(), "{4}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void freeNoncreatureSpellStillRequiresEnoughLands() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        var asForetold = harness.addToBattlefieldAndReturn(player2, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 4);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersFreeNoncreatureSpellWhenLandRestrictionIsSatisfied() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Plains());
        }
        var asForetold = harness.addToBattlefieldAndReturn(player2, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 4);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wrath of God");
        harness.assertOnBattlefield(player1, "Lavinia, Azorius Renegade");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void freeSpellIsStillCounteredAfterLaviniaLeavesBattlefield() {
        var lavinia = harness.addToBattlefieldAndReturn(player1, new LaviniaAzoriusRenegade());
        var asForetold = harness.addToBattlefieldAndReturn(player2, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);

        harness.setHand(player1, List.of(new Electrodominance()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 2, lavinia.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Lavinia, Azorius Renegade");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsChosenXInOpponentsNoncreatureSpellManaValue() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player2, List.of(new Electrodominance()));
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void allowsChosenXWhenSpellManaValueEqualsOpponentsLandCount() {
        harness.addToBattlefield(player1, new LaviniaAzoriusRenegade());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Plains());
        }
        harness.setHand(player2, List.of(new Electrodominance()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, 1, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player2, "Electrodominance");
    }
}
