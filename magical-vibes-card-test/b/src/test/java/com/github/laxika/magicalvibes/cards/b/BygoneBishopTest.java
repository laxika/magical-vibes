package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BygoneBishop.class, GrizzlyBears.class, HillGiant.class, Murder.class, WindDrake.class})
class BygoneBishopTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when its controller casts a creature spell with mana value 3 or less")
    void investigatesOnSmallCreatureSpell() {
        harness.addToBattlefield(player1, new BygoneBishop());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates for a creature spell with mana value exactly 3")
    void investigatesAtManaValueThree() {
        harness.addToBattlefield(player1, new BygoneBishop());
        harness.setHand(player1, List.of(new WindDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate for a creature spell with mana value greater than 3")
    void doesNotInvestigateForLargeCreatureSpell() {
        harness.addToBattlefield(player1, new BygoneBishop());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Does not investigate for a noncreature spell")
    void doesNotInvestigateForNoncreatureSpell() {
        harness.addToBattlefield(player1, new BygoneBishop());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Does not investigate when an opponent casts a small creature spell")
    void doesNotInvestigateForOpponentCreatureSpell() {
        harness.addToBattlefield(player1, new BygoneBishop());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Casting Bygone Bishop does not trigger its own ability")
    void doesNotInvestigateForItsOwnCast() {
        harness.setHand(player1, List.of(new BygoneBishop()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bygone Bishop");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Putting a creature onto the battlefield without casting does not investigate")
    void doesNotInvestigateForCreatureEnteringWithoutCast() {
        harness.addToBattlefield(player1, new BygoneBishop());
        harness.addToBattlefield(player1, new BygoneBishop());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Each Bishop investigates before the qualifying creature resolves")
    void eachBishopInvestigatesBeforeCreatureResolves() {
        harness.addToBattlefield(player1, new BygoneBishop());
        harness.addToBattlefield(player1, new BygoneBishop());
        harness.setHand(player1, List.of(new BygoneBishop()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(findPermanents(player1, "Bygone Bishop")).hasSize(2);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bygone Bishop")).hasSize(3);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("An investigated Clue can be sacrificed for two mana to draw one card")
    void investigatedClueDrawsOneCard() {
        harness.addToBattlefield(player1, new BygoneBishop());
        harness.setHand(player1, List.of(new BygoneBishop()));
        harness.setLibrary(player1, List.of(new BygoneBishop(), new BygoneBishop()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
