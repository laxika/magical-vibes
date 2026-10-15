package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanguineBond.class, AngelOfMercy.class, SoulWarden.class, GrizzlyBears.class,
        ChildOfNight.class, Naturalize.class, StrionicResonator.class})
class SanguineBondTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent loses life equal to life gained from ETB effect")
    void opponentLosesLifeOnLifeGain() {
        harness.addToBattlefield(player1, new SanguineBond());

        int opponentLifeBefore = gd.getLife(player2.getId());

        // Angel of Mercy ETB: gain 3 life
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23); // 20 + 3
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    @DisplayName("Does not trigger when opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        harness.addToBattlefield(player1, new SanguineBond());

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        // Opponent gains life
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        // Player 1's life should be unchanged, player 2 gained 3 life
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore + 3);
    }

    @Test
    @DisplayName("Multiple Sanguine Bonds each trigger independently")
    void multipleSanguineBondsEachTrigger() {
        harness.addToBattlefield(player1, new SanguineBond());
        harness.addToBattlefield(player1, new SanguineBond());

        int opponentLifeBefore = gd.getLife(player2.getId());

        // Angel of Mercy ETB: gain 3 life
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23); // 20 + 3
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 6); // 3 + 3
    }

    @Test
    @DisplayName("Life loss amount matches exact life gained")
    void lifeLossMatchesExactLifeGained() {
        harness.addToBattlefield(player1, new SanguineBond());

        // Use Soul Warden + creature for 1 life gain
        harness.addToBattlefield(player1, new SoulWarden());

        int opponentLifeBefore = gd.getLife(player2.getId());

        // Cast a creature — Soul Warden triggers (gain 1 life)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21); // 20 + 1
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Lifelink triggers life loss in addition to combat damage")
    void triggersFromLifelink() {
        harness.addToBattlefield(player1, new SanguineBond());
        addCreatureReady(player1, new ChildOfNight());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A pending trigger resolves after Sanguine Bond is destroyed")
    void pendingTriggerSurvivesSourceRemoval() {
        var bond = harness.addToBattlefieldAndReturn(player1, new SanguineBond());
        harness.setHand(player1, List.of(new AngelOfMercy(), new Naturalize()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, bond.getId());
        harness.assertNotOnBattlefield(player1, "Sanguine Bond");
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Separate life gains retain their individual amounts")
    void separateLifeGainEventsRetainTheirAmounts() {
        harness.addToBattlefield(player1, new SanguineBond());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A copied trigger cannot be retargeted to its controller")
    void copiedTriggerCannotTargetItsController() {
        harness.addToBattlefield(player1, new SanguineBond());
        harness.addToBattlefield(player1, new StrionicResonator());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        var triggerCardId = gd.stack.getLast().getCard().getId();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, triggerCardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 14);
    }
}
