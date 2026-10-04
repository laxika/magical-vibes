package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirebladeArtist.class, SauroformHybrid.class, DovinGrandArbiter.class})
class FirebladeArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature deals 2 damage to an opponent")
    void sacrificesCreatureAndDealsDamageToOpponent() {
        harness.addToBattlefield(player1, new FirebladeArtist());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sauroform Hybrid");
        harness.assertInGraveyard(player1, "Sauroform Hybrid");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Declining the sacrifice deals no damage")
    void decliningSacrificeDoesNothing() {
        harness.addToBattlefield(player1, new FirebladeArtist());
        harness.addToBattlefield(player1, new SauroformHybrid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Sauroform Hybrid");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The damage trigger can target an opponent's planeswalker")
    void dealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new FirebladeArtist());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(planeswalker.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fireblade Artist can sacrifice itself and damage resolves separately")
    void canSacrificeItself() {
        Permanent artist = harness.addToBattlefieldAndReturn(player1, new FirebladeArtist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artist.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.assertInGraveyard(player1, "Fireblade Artist");
        harness.assertNotOnBattlefield(player1, "Fireblade Artist");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The damage trigger cannot target its controller or creatures")
    void damageTargetsExcludeControllerAndCreatures() {
        harness.addToBattlefield(player1, new FirebladeArtist());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(player2.getId())
                .doesNotContain(player1.getId(), opposingCreature.getId());
    }

    @Test
    @DisplayName("The damage trigger can target its controller's planeswalker")
    void canDamageOwnPlaneswalker() {
        Permanent artist = harness.addToBattlefieldAndReturn(player1, new FirebladeArtist());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new DovinGrandArbiter());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artist.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new FirebladeArtist());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Fireblade Artist");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Only creatures controlled by the ability's controller can be sacrificed")
    void cannotSacrificeOpponentsCreature() {
        Permanent artist = harness.addToBattlefieldAndReturn(player1, new FirebladeArtist());
        harness.addToBattlefield(player2, new SauroformHybrid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(artist.getId());
        harness.handlePermanentChosen(player1, artist.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sauroform Hybrid");
        harness.assertLife(player2, 18);
    }
}
