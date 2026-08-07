package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SamuraiEnforcers;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OathkeeperTakenosDaishoTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addPermanent(player1, new GrizzlyBears());
        Permanent oathkeeper = addPermanent(player1, new OathkeeperTakenosDaisho());
        oathkeeper.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Unequipped creature gets no boost")
    void unequippedCreatureNoBoost() {
        Permanent creature = addPermanent(player1, new GrizzlyBears());
        addPermanent(player1, new OathkeeperTakenosDaisho());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped Samurai returns to the battlefield under your control when it dies")
    void samuraiReturnsToBattlefield() {
        Permanent creature = addPermanent(player1, new SamuraiEnforcers());
        Permanent oathkeeper = addPermanent(player1, new OathkeeperTakenosDaisho());
        oathkeeper.setAttachedTo(creature.getId());

        killCreature(creature);

        harness.assertOnBattlefield(player1, "Samurai Enforcers");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Samurai Enforcers"));
    }

    @Test
    @DisplayName("Equipped non-Samurai stays in the graveyard when it dies")
    void nonSamuraiStaysInGraveyard() {
        Permanent creature = addPermanent(player1, new GrizzlyBears());
        Permanent oathkeeper = addPermanent(player1, new OathkeeperTakenosDaisho());
        oathkeeper.setAttachedTo(creature.getId());

        killCreature(creature);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Unequipped Samurai is not returned")
    void unequippedSamuraiNotReturned() {
        Permanent creature = addPermanent(player1, new SamuraiEnforcers());
        addPermanent(player1, new OathkeeperTakenosDaisho());

        killCreature(creature);

        harness.assertNotOnBattlefield(player1, "Samurai Enforcers");
    }

    @Test
    @DisplayName("Equipped creature is exiled when Oathkeeper is destroyed")
    void equippedCreatureExiledWhenOathkeeperDies() {
        Permanent creature = addPermanent(player1, new GrizzlyBears());
        Permanent oathkeeper = addPermanent(player1, new OathkeeperTakenosDaisho());
        oathkeeper.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, oathkeeper.getId());
        harness.passBothPriorities(); // resolve Shatter
        harness.passBothPriorities(); // resolve the exile trigger

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Nothing is exiled when an unattached Oathkeeper is destroyed")
    void nothingExiledWhenUnattached() {
        Permanent creature = addPermanent(player1, new GrizzlyBears());
        Permanent oathkeeper = addPermanent(player1, new OathkeeperTakenosDaisho());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, oathkeeper.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(creature).isNotNull();
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Deathmark — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve death trigger (if any)
    }
}
