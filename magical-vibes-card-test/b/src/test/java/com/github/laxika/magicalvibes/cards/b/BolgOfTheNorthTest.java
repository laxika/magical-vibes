package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({BolgOfTheNorth.class, AirElemental.class, GrizzlyBears.class})
class BolgOfTheNorthTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing deals damage equal to the sacrificed creature's power and amasses the excess")
    void sacrificeDealsSacrificedPowerDamageAndAmassesExcess() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(sacrificed, target);

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(PermanentGrants.subtypesOf(gd, army)).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("No excess damage means no amass")
    void noExcessDamageMeansNoAmass() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castAndResolve(sacrificed, target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BolgOfTheNorth()));
        addManaForBolg();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrificed);
    }

    @Test
    @DisplayName("The damage cannot target Bolg itself")
    void targetCreatureCannotBeBolgItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent bolg = castToSacrificeChoice();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(bolg.getId());
    }

    @Test
    @DisplayName("Exactly lethal damage does not amass")
    void exactlyLethalDamageDoesNotAmass() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(sacrificed, target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
    }

    @Test
    @DisplayName("Previously marked damage increases the excess")
    void previouslyMarkedDamageIncreasesExcess() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setMarkedDamage(1);
        castAndResolve(sacrificed, target);
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(findPermanent(player1, "Goblin Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the sacrificed creature's modified power")
    void modifiedPowerIsRememberedAfterSacrifice() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrificed.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castAndResolve(sacrificed, target);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(findPermanent(player1, "Goblin Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can damage another creature you control")
    void canDamageAnotherCreatureYouControl() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAndResolve(sacrificed, target);
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Goblin Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castAndResolve(Permanent sacrificed, Permanent target) {
        castToSacrificeChoice();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiPermanentChoice) {
            harness.handleMultiplePermanentsChosen(player1, List.of(sacrificed.getId()));
        }
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private Permanent castToSacrificeChoice() {
        harness.setHand(player1, List.of(new BolgOfTheNorth()));
        addManaForBolg();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        return findPermanent(player1, "Bolg of the North");
    }

    private void addManaForBolg() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
