package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.cards.p.PersistentSpecimen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotTideGargantua.class, PersistentSpecimen.class, HerosDownfall.class})
class RotTideGargantuaTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Rot-Tide Gargantua and the other creature on the battlefield")
    void decliningExploitDoesNothing() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        castRotTideGargantua();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Rot-Tide Gargantua");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    @Test
    @DisplayName("Exploiting a creature makes each opponent sacrifice a creature")
    void exploitMakesEachOpponentSacrifice() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());

        castRotTideGargantua();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, sacrificed.getId());

        harness.assertOnBattlefield(player1, "Rot-Tide Gargantua");
        harness.assertInGraveyard(player1, "Persistent Specimen");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(remaining);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sacrificed);
    }

    @Test
    @DisplayName("Exploiting Rot-Tide Gargantua itself still makes each opponent sacrifice")
    void exploitingItselfStillTriggers() {
        harness.addToBattlefield(player2, new PersistentSpecimen());

        castRotTideGargantua();
        Permanent gargantua = findPermanent(player1, "Rot-Tide Gargantua");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, gargantua.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rot-Tide Gargantua");
        harness.assertInGraveyard(player2, "Persistent Specimen");
    }

    @Test
    @DisplayName("Exploit still sacrifices your creature when the opponent has no creatures")
    void exploitWithNoOpponentCreatures() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        castRotTideGargantua();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Persistent Specimen");
        harness.assertOnBattlefield(player1, "Rot-Tide Gargantua");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Removing Gargantua before exploit resolves prevents the opponent sacrifice")
    void removedBeforeExploitDoesNotTrigger() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RotTideGargantua(), "{3}{B}{B}");
        harness.passBothPriorities();

        destroyGargantua();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertInGraveyard(player1, "Rot-Tide Gargantua");
        harness.assertInGraveyard(player1, "Persistent Specimen");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Gargantua after exploiting does not stop the opponent sacrifice")
    void removedAfterExploitStillSacrifices() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        harness.addToBattlefield(player2, new PersistentSpecimen());

        castRotTideGargantua();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        destroyGargantua();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rot-Tide Gargantua");
        harness.assertInGraveyard(player2, "Persistent Specimen");
    }

    private void destroyGargantua() {
        harness.setHand(player2, List.of(new HerosDownfall()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Rot-Tide Gargantua"));
    }

    private void castRotTideGargantua() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RotTideGargantua(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
