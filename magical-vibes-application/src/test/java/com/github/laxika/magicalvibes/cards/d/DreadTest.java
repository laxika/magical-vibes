package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FlamekinSpitfire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Thoughtseize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dread.class, GrizzlyBears.class, FlamekinSpitfire.class, ChandraNalaar.class, Thoughtseize.class})
class DreadTest extends BaseCardTest {

    @Test
    @DisplayName("Creature that deals combat damage to Dread's controller is destroyed")
    void damageSourceIsDestroyed() {
        harness.addToBattlefield(player2, new Dread());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
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
        harness.setHand(player2, List.of(dread, new GrizzlyBears()));
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
}
