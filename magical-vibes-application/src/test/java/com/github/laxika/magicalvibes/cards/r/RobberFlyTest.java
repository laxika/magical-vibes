package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RobberFly.class, DeadlyInsect.class, Forest.class, RishadanAirship.class,
        JaceBeleren.class, Boomerang.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
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

    @Test
    @DisplayName("Attacking a planeswalker refreshes its controller's hand")
    void blockedWhileAttackingPlaneswalker() {
        Card discarded = new Forest();
        Card drawn = new DeadlyInsect();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(drawn));
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addAttackingRobberFly().setAttackTarget(planeswalker.getId());
        addCreatureReady(player2, new RishadanAirship());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("The defending player still discards when the attacked planeswalker leaves")
    void attackedPlaneswalkerLeavesBeforeTriggerResolves() {
        Card discarded = new Forest();
        Card firstDraw = new DeadlyInsect();
        Card secondDraw = new Forest();
        JaceBeleren jace = new JaceBeleren();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, jace);
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addAttackingRobberFly().setAttackTarget(planeswalker.getId());
        addCreatureReady(player2, new RishadanAirship());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, planeswalker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded, jace);
    }

    @Test
    @DisplayName("Attacking a battle refreshes its protector's hand, not its controller's")
    void blockedWhileAttackingBattle() {
        Card controllerHand = new Forest();
        Card discarded = new DeadlyInsect();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(controllerHand));
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(drawn));
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());
        addAttackingRobberFly().setAttackTarget(battle.getId());
        addCreatureReady(player2, new RishadanAirship());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The hand refresh resolves even if Robber Fly leaves the battlefield")
    void sourceLeavesBeforeTriggerResolves() {
        Card discarded = new Forest();
        Card drawn = new DeadlyInsect();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(drawn));
        Permanent fly = addAttackingRobberFly();
        addCreatureReady(player2, new RishadanAirship());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, fly.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fly);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fly.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    private Permanent addAttackingRobberFly() {
        Permanent perm = addCreatureReady(player1, new RobberFly());
        perm.setAttacking(true);
        perm.setAttackTarget(player2.getId());
        return perm;
    }
}
