package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RobberFly.class, DeadlyInsect.class, Forest.class, RishadanAirship.class})
class RobberFlyTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked makes the defending player replace their hand")
    void blockedDefendingPlayerDiscardsAndDrawsThatMany() {
        Card discardedCreature = new DeadlyInsect();
        Card discardedLand = new Forest();
        Card drawnCreature = new DeadlyInsect();
        Card drawnLand = new Forest();
        Card controllerHandCard = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(discardedCreature, discardedLand)));
        harness.setLibrary(player2, new ArrayList<>(List.of(drawnCreature, drawnLand)));
        harness.setHand(player1, List.of(controllerHandCard));
        addAttackingRobberFly();
        addCreatureReady(player2, new RishadanAirship());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCreature, drawnLand);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerHandCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(discardedCreature, discardedLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty defending hand causes no draw")
    void blockedWithEmptyDefendingHandDoesNothing() {
        Card libraryCard = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(libraryCard));
        addAttackingRobberFly();
        addCreatureReady(player2, new RishadanAirship());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple blockers still cause only one hand refresh")
    void blockedByMultipleCreaturesTriggersOnce() {
        Card discardedCreature = new DeadlyInsect();
        Card discardedLand = new Forest();
        Card drawnCreature = new DeadlyInsect();
        Card drawnLand = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(discardedCreature, discardedLand)));
        harness.setLibrary(player2, new ArrayList<>(List.of(drawnCreature, drawnLand)));
        addAttackingRobberFly();
        addCreatureReady(player2, new RishadanAirship());
        addCreatureReady(player2, new RishadanAirship());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCreature, drawnLand);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(discardedCreature, discardedLand);
    }

    @Test
    @DisplayName("An unblocked Robber Fly does not refresh the defending hand")
    void unblockedDoesNothing() {
        Card handCard = new DeadlyInsect();
        harness.setHand(player2, List.of(handCard));
        addAttackingRobberFly();

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).contains(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void addAttackingRobberFly() {
        Permanent perm = new Permanent(new RobberFly());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(perm);
    }
}
