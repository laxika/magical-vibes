package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodcallerScourge.class, Divination.class, GrizzlyBears.class, HillGiant.class, ShivanDragon.class})
class BroodcallerScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Dragon combat damage offers only permanent cards within the damage limit")
    void combatDamageFiltersHandByPermanentTypeAndDamage() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new ShivanDragon(), new Divination()));
        addCreatureReady(player1, new BroodcallerScourge());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Accepting the trigger puts the chosen permanent onto the battlefield")
    void acceptingTriggerPutsPermanentOntoBattlefield() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new BroodcallerScourge());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the trigger leaves the hand unchanged")
    void decliningTriggerLeavesHandUnchanged() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new BroodcallerScourge());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-Dragons do not cause the trigger")
    void nonDragonCombatDamageDoesNotTrigger() {
        harness.setHand(player1, List.of(new HillGiant()));
        addCreatureReady(player1, new BroodcallerScourge());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Simultaneous Dragon damage is combined into one trigger per Scourge")
    void simultaneousDragonDamageIsCombined() {
        harness.setHand(player1, List.of(new BroodcallerScourge(), new BroodcallerScourge()));
        addCreatureReady(player1, new BroodcallerScourge());
        addCreatureReady(player1, new ShivanDragon());
        addCreatureReady(player1, new ShivanDragon());

        declareAttackers(List.of(1, 2));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Broodcaller Scourge")).isEqualTo(2);
        harness.assertInHand(player1, "Broodcaller Scourge");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A permanent with mana value equal to the damage is eligible")
    @CardUsed({BroodcallerScourge.class})
    void manaValueEqualToDamageIsEligible() {
        harness.setHand(player1, List.of(new BroodcallerScourge()));
        addCreatureReady(player1, new BroodcallerScourge())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Broodcaller Scourge")).isEqualTo(2);
        harness.assertNotInHand(player1, "Broodcaller Scourge");
        assertThat(findPermanents(player1, "Broodcaller Scourge").get(1).isTapped()).isFalse();
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Damage from non-Dragons is excluded from the mana value limit")
    void nonDragonDamageIsExcludedFromLimit() {
        harness.setHand(player1, List.of(new BroodcallerScourge()));
        addCreatureReady(player1, new BroodcallerScourge());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 13);
        harness.assertInHand(player1, "Broodcaller Scourge");
        assertThat(countPermanents(player1, "Broodcaller Scourge")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opposing Dragon does not trigger a Scourge you control")
    void opposingDragonDoesNotTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new BroodcallerScourge());
        addCreatureReady(player2, new ShivanDragon());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with an empty hand finishes without a card choice")
    @CardUsed({BroodcallerScourge.class})
    void acceptingWithEmptyHandFinishes() {
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new BroodcallerScourge());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Broodcaller Scourge")).isEqualTo(1);
    }
}
