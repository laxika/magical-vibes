package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BolgOfTheNorth.class, AirElemental.class, GrizzlyBears.class})
class BolgOfTheNorthTest extends BaseCardTest {

    @Test
    void sacrificeDealsSacrificedPowerDamageAndAmassesExcess() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(sacrificed, target);

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    void noExcessDamageMeansNoAmass() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castAndResolve(sacrificed, target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
    }

    @Test
    void decliningSacrificeDoesNothing() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BolgOfTheNorth()));
        addManaForBolg();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrificed);
    }

    @Test
    void targetCreatureCannotBeBolgItself() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent bolg = castToSacrificeChoice();

        harness.handlePermanentChosen(player1, sacrificed.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(bolg.getId());
    }

    private void castAndResolve(Permanent sacrificed, Permanent target) {
        castToSacrificeChoice();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private Permanent castToSacrificeChoice() {
        harness.setHand(player1, List.of(new BolgOfTheNorth()));
        addManaForBolg();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        return findPermanent(player1, "Bolg of the North");
    }

    private void addManaForBolg() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
