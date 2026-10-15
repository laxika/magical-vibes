package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VaporSnare.class, Forest.class, GrizzlyBears.class})
class VaporSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Vapor Snare gains control of the enchanted creature")
    void gainsControlOfEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castVaporSnare(creature);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Returning a land during upkeep keeps Vapor Snare attached")
    void returningLandKeepsAuraAttached() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        castVaporSnare(creature);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId()).doesNotContain(opposingLand.getId());

        harness.handlePermanentChosen(player1, land.getId());

        harness.assertOnBattlefield(player1, "Vapor Snare");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(findPermanent(player1, "Vapor Snare"));
    }

    @Test
    @DisplayName("Declining to return a land sacrifices Vapor Snare")
    void decliningToReturnLandSacrificesAura() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new Forest());

        castVaporSnare(creature);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Vapor Snare");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Having no land during upkeep sacrifices Vapor Snare")
    void noLandSacrificesAura() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castVaporSnare(creature);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vapor Snare");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Opponent upkeep does not require returning a land")
    void opponentUpkeepDoesNotTriggerPayment() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castVaporSnare(creature);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Vapor Snare");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Payment is required again on the next controller upkeep")
    void paymentIsRequiredEveryUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castVaporSnare(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());

        advanceToUpkeep(player2);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vapor Snare");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("A borrowed land pays upkeep and returns to its owner's hand")
    void borrowedLandReturnsToOwner() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.stolenCreatures.put(land.getId(), player2.getId());
        castVaporSnare(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Vapor Snare");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(land);
    }

    private void castVaporSnare(Permanent creature) {
        harness.setHand(player1, List.of(new VaporSnare()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
