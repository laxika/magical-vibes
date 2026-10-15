package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FlamekinSpitfire;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.t.Thoughtseize;
import com.github.laxika.magicalvibes.cards.t.TimberProtector;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dread.class, WoodlandChangeling.class, FlamekinSpitfire.class, ChandraNalaar.class,
        Thoughtseize.class, JaceBeleren.class, TimberProtector.class})
class DreadTest extends BaseCardTest {

    @Test
    @DisplayName("Creature that deals combat damage to Dread's controller is destroyed")
    void damageSourceIsDestroyed() {
        harness.addToBattlefield(player2, new Dread());
        Permanent attacker = addCreatureReady(player1, new WoodlandChangeling());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Woodland Changeling");
        harness.assertInGraveyard(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("Creature that deals noncombat damage to Dread's controller is destroyed")
    void noncombatDamageSourceIsDestroyed() {
        harness.addToBattlefield(player2, new Dread());
        addCreatureReady(player1, new FlamekinSpitfire());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Flamekin Spitfire");
        harness.assertInGraveyard(player1, "Flamekin Spitfire");
    }

    @Test
    @DisplayName("Damage from a noncreature permanent does not destroy its source")
    void noncreatureDamageSourceIsNotDestroyed() {
        harness.addToBattlefield(player2, new Dread());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Chandra Nalaar");
    }

    @Test
    @DisplayName("When Dread is discarded from hand it is shuffled into its owner's library")
    void discardedFromHandShufflesIntoLibrary() {
        Dread dread = new Dread();
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(dread, new WoodlandChangeling()));
        harness.setHand(player1, List.of(new Thoughtseize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertNotInHand(player2, "Dread");
        harness.assertNotInGraveyard(player2, "Dread");
        assertThat(gd.playerDecks.get(player2.getId())).anyMatch(card -> card.getId().equals(dread.getId()));
    }

    @Test
    @DisplayName("When Dread is put into a graveyard it is shuffled into its owner's library")
    void putIntoGraveyardShufflesIntoLibrary() {
        harness.setLibrary(player2, List.of());
        Permanent dread = harness.addToBattlefieldAndReturn(player2, new Dread());
        dread.setMarkedDamage(6);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotInGraveyard(player2, "Dread");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dread"));
    }

    @Test
    void damageToOtherPlayerDoesNotTrigger() {
        harness.addToBattlefield(player1, new Dread());
        addCreatureReady(player1, new FlamekinSpitfire());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Flamekin Spitfire");
    }

    @Test
    void ownCreatureDealingDamageToControllerIsDestroyed() {
        harness.addToBattlefield(player1, new Dread());
        addCreatureReady(player1, new FlamekinSpitfire());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Flamekin Spitfire");
        harness.assertNotOnBattlefield(player1, "Flamekin Spitfire");
    }

    @Test
    void eachCreatureDealingCombatDamageIsDestroyed() {
        harness.addToBattlefield(player2, new Dread());
        addCreatureReady(player1, new WoodlandChangeling()).setAttacking(true);
        addCreatureReady(player1, new WoodlandChangeling()).setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertNotOnBattlefield(player1, "Woodland Changeling");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof WoodlandChangeling).hasSize(2);
    }

    @Test
    void indestructibleDamageSourceSurvives() {
        harness.addToBattlefield(player2, new Dread());
        addCreatureReady(player1, new WoodlandChangeling()).setAttacking(true);
        harness.addToBattlefield(player1, new TimberProtector());

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Woodland Changeling");
        harness.assertNotInGraveyard(player1, "Woodland Changeling");
    }

    @Test
    void damageToDreadItselfDoesNotTrigger() {
        Permanent dread = harness.addToBattlefieldAndReturn(player2, new Dread());
        addCreatureReady(player1, new FlamekinSpitfire());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, dread.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(dread.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Flamekin Spitfire");
    }

    @Test
    void milledDreadShufflesOnlyItselfIntoLibrary() {
        Dread dread = new Dread();
        WoodlandChangeling otherCard = new WoodlandChangeling();
        harness.setLibrary(player2, List.of(dread, otherCard));
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 11);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotInGraveyard(player2, "Dread");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(dread);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(otherCard);
    }

    @Test
    void fearRejectsGreenBlocker() {
        addCreatureReady(player1, new Dread());
        addCreatureReady(player2, new WoodlandChangeling());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("(fear)");
    }

    @Test
    void fearAllowsBlackBlocker() {
        addCreatureReady(player1, new Dread());
        Permanent blocker = addCreatureReady(player2, new Dread());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
