package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.cards.m.MetropolisSprite;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrakewingKrasis.class, ArmoredTransport.class, MetropolisSprite.class})
class DrakewingKrasisTest extends BaseCardTest {

    @Test
    void nonflyingCreatureCannotBlock() {
        addCreatureReady(player1, new DrakewingKrasis());
        addCreatureReady(player2, new ArmoredTransport());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void trampleDealsExcessDamageEvenWhenAttackerDies() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DrakewingKrasis());
        Permanent blocker = addCreatureReady(player2, new MetropolisSprite());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Drakewing Krasis");
        harness.assertInGraveyard(player2, "Metropolis Sprite");
        harness.assertNotOnBattlefield(player1, "Drakewing Krasis");
        harness.assertNotOnBattlefield(player2, "Metropolis Sprite");
    }

    @Test
    void unblockedAttackDealsFullCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DrakewingKrasis());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    void flyingDoesNotPreventBlockingGroundCreatures() {
        addCreatureReady(player1, new ArmoredTransport());
        Permanent blocker = addCreatureReady(player2, new DrakewingKrasis());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
