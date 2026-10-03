package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallForthTheTempest.class, GrizzlyBears.class, HillGiant.class, Mountain.class, Blaze.class})
class CallForthTheTempestTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the other spells' total mana value to opposing creatures only")
    void damagesOpposingCreaturesByOtherSpellsManaValue() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castGrizzlyBears();
        castCallForthTheTempestWithLandsOnly();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves both Cascade abilities before the spell")
    void cascadesTwice() {
        harness.setLibrary(player1, List.of(new HillGiant(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CallForthTheTempest()));
        addCallForthMana();
        harness.castSorcery(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting("name").containsExactly("Hill Giant");

        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting("name").containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Deals no damage when no other spells were cast")
    void noOtherSpellsMeansNoDamage() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castCallForthTheTempestWithLandsOnly();
        assertThat(opposingCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Call Forth the Tempest");
    }

    @Test
    @DisplayName("Both cascade spells resolve before the damage and contribute their mana values")
    void includesBothCascadeSpells() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new HillGiant(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CallForthTheTempest()));
        addCallForthMana();
        harness.castSorcery(player1, 0);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An earlier X spell contributes its chosen X to its mana value")
    void includesChosenXOfEarlierSpell() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        castCallForthTheTempestWithLandsOnly();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player2, 18);
    }

    private void castGrizzlyBears() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void castCallForthTheTempestWithLandsOnly() {
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setHand(player1, List.of(new CallForthTheTempest()));
        addCallForthMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCallForthMana() {
        harness.addMana(player1, ManaColor.RED, 8);
    }
}
