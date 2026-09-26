package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.j.JukaiMessenger;
import com.github.laxika.magicalvibes.cards.p.PullUnder;
import com.github.laxika.magicalvibes.cards.s.SamuraiEnforcers;
import com.github.laxika.magicalvibes.cards.w.WearAway;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathkeeperTakenosDaisho.class, SamuraiEnforcers.class, JukaiMessenger.class,
        PullUnder.class, WearAway.class})
class OathkeeperTakenosDaishoTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {2} attaches Oathkeeper to a creature you control")
    void equipsToControlledCreature() {
        Permanent oathkeeper = harness.addToBattlefieldAndReturn(player1, new OathkeeperTakenosDaisho());
        Permanent creature = addCreatureReady(player1, new JukaiMessenger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(oathkeeper.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature gets +3/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new JukaiMessenger());
        Permanent oathkeeper = harness.addToBattlefieldAndReturn(player1, new OathkeeperTakenosDaisho());
        oathkeeper.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unequipped creature gets no boost")
    void unequippedCreatureNoBoost() {
        Permanent creature = addCreatureReady(player1, new JukaiMessenger());
        harness.addToBattlefield(player1, new OathkeeperTakenosDaisho());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipped Samurai returns to the battlefield under your control when it dies")
    void samuraiReturnsToBattlefield() {
        Permanent creature = addCreatureReady(player1, new SamuraiEnforcers());
        Permanent oathkeeper = harness.addToBattlefieldAndReturn(player1, new OathkeeperTakenosDaisho());
        oathkeeper.setAttachedTo(creature.getId());

        killCreature(creature);

        harness.assertOnBattlefield(player1, "Samurai Enforcers");
        harness.assertNotInGraveyard(player1, "Samurai Enforcers");
        assertThat(oathkeeper.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equipped non-Samurai stays in the graveyard when it dies")
    void nonSamuraiStaysInGraveyard() {
        Permanent creature = addCreatureReady(player1, new JukaiMessenger());
        Permanent oathkeeper = harness.addToBattlefieldAndReturn(player1, new OathkeeperTakenosDaisho());
        oathkeeper.setAttachedTo(creature.getId());

        killCreature(creature);

        harness.assertNotOnBattlefield(player1, "Jukai Messenger");
        harness.assertInGraveyard(player1, "Jukai Messenger");
    }

    @Test
    @DisplayName("Unequipped Samurai is not returned")
    void unequippedSamuraiNotReturned() {
        Permanent creature = addCreatureReady(player1, new SamuraiEnforcers());
        harness.addToBattlefield(player1, new OathkeeperTakenosDaisho());

        killCreature(creature);

        harness.assertNotOnBattlefield(player1, "Samurai Enforcers");
    }

    @Test
    @DisplayName("Equipped Samurai returns under Oathkeeper's controller's control")
    void samuraiReturnsUnderOathkeepersController() {
        Permanent creature = addCreatureReady(player2, new SamuraiEnforcers());
        Permanent oathkeeper = harness.addToBattlefieldAndReturn(player1, new OathkeeperTakenosDaisho());
        oathkeeper.setAttachedTo(creature.getId());

        killCreature(creature);

        harness.assertOnBattlefield(player1, "Samurai Enforcers");
        harness.assertNotOnBattlefield(player2, "Samurai Enforcers");
    }

    @Test
    @DisplayName("Equipped creature is exiled when Oathkeeper is destroyed")
    void equippedCreatureExiledWhenOathkeeperDies() {
        Permanent creature = addCreatureReady(player1, new JukaiMessenger());
        Permanent oathkeeper = harness.addToBattlefieldAndReturn(player1, new OathkeeperTakenosDaisho());
        oathkeeper.setAttachedTo(creature.getId());

        destroyOathkeeper(oathkeeper);
        harness.passBothPriorities(); // resolve the exile trigger

        harness.assertNotOnBattlefield(player1, "Jukai Messenger");
        harness.assertNotInGraveyard(player1, "Jukai Messenger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Jukai Messenger"));
    }

    @Test
    @DisplayName("Nothing is exiled when an unattached Oathkeeper is destroyed")
    void nothingExiledWhenUnattached() {
        addCreatureReady(player1, new JukaiMessenger());
        Permanent oathkeeper = harness.addToBattlefieldAndReturn(player1, new OathkeeperTakenosDaisho());

        destroyOathkeeper(oathkeeper);

        harness.assertOnBattlefield(player1, "Jukai Messenger");
    }

    private void destroyOathkeeper(Permanent oathkeeper) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WearAway()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, oathkeeper.getId());
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new PullUnder()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Pull Under — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve death trigger (if any)
    }
}
