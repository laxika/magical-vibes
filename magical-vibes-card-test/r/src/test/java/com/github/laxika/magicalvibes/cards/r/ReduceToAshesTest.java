package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReduceToAshes.class, GreenwoodSentinel.class, Vorstclaw.class, Murder.class})
class ReduceToAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a small creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        UUID targetId = harness.getPermanentId(player2, "Greenwood Sentinel");
        harness.setHand(player1, List.of(new ReduceToAshes()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertNotInGraveyard(player2, "Greenwood Sentinel");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Greenwood Sentinel"));
    }

    @Test
    @DisplayName("Deals 5 damage to a surviving creature and marks it for exile if it dies this turn")
    void marksSurvivorForExile() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        UUID targetId = harness.getPermanentId(player2, "Vorstclaw");
        harness.setHand(player1, List.of(new ReduceToAshes()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(creature.getMarkedDamage()).isEqualTo(5);
        assertThat(creature.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ReduceToAshes()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A surviving creature is exiled when destroyed later in the same turn")
    void exilesSurvivorDestroyedLaterThisTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        harness.setHand(player1, List.of(new ReduceToAshes(), new Murder()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Vorstclaw");
        harness.assertNotInGraveyard(player2, "Vorstclaw");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Vorstclaw"));
    }

    @Test
    @DisplayName("The exile replacement expires after the turn")
    void doesNotExileCreatureDestroyedNextTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        harness.setHand(player1, List.of(new ReduceToAshes(), new Murder()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Vorstclaw");
        harness.assertInGraveyard(player2, "Vorstclaw");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Vorstclaw"));
    }

    @Test
    @DisplayName("A creature destroyed in response goes to the graveyard before the replacement exists")
    void targetRemovedBeforeResolutionIsNotExiled() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new ReduceToAshes()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, creature.getId());
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Greenwood Sentinel"));
        harness.assertInGraveyard(player1, "Reduce to Ashes");
        assertThat(gd.stack).isEmpty();
    }
}
