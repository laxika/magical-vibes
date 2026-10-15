package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JukaiLiberator.class, Forest.class, GrizzlyBears.class, Shock.class})
class JukaiLiberatorTest extends BaseCardTest {

    @Test
    void seeksAChosenLand() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, bears, shock));
        addCreatureReady(player1, new JukaiLiberator());

        dealCombatDamageAndChoose("Land");

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, shock);
    }

    @Test
    void seeksAChosenNonlandPermanent() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, bears, shock));
        addCreatureReady(player1, new JukaiLiberator());

        dealCombatDamageAndChoose("Nonland");

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, shock);
    }

    @Test
    void choosingLandWithNoLandDoesNotSeekAnotherKind() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(bears, shock));
        addCreatureReady(player1, new JukaiLiberator());

        dealCombatDamageAndChoose("Land");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears, shock);
    }

    @Test
    void choosingNonlandWithNoNonlandPermanentDoesNotSeekAnInstant() {
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest, shock));
        addCreatureReady(player1, new JukaiLiberator());

        dealCombatDamageAndChoose("Nonland");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, shock);
    }

    @Test
    void seekingPreservesTheOrderOfTheRemainingLibrary() {
        Card first = new Shock();
        Card forest = new Forest();
        Card last = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, forest, last));
        addCreatureReady(player1, new JukaiLiberator());

        dealCombatDamageAndChoose("Land");

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, last);
    }

    @Test
    void combatDamageToACreatureDoesNotSeek() {
        Card forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        addCreatureReady(player1, new JukaiLiberator());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ninjutsuReturnsTheAttackerAndEntersTappedAttackingTheSamePlayer() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.setHand(player1, List.of(new JukaiLiberator()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of());
            harness.activateHandAbility(player1, 0, attacker.getId());
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Jukai Liberator");
            harness.passBothPriorities();
        });

        Permanent liberator = findPermanent(player1, "Jukai Liberator");
        assertThat(liberator.isTapped()).isTrue();
        assertThat(liberator.isAttacking()).isTrue();
        assertThat(liberator.getAttackTarget()).isEqualTo(player2.getId());
        harness.assertNotInHand(player1, "Jukai Liberator");
    }

    private void dealCombatDamageAndChoose(String choice) {
        declareAttackers(List.of(0));
        resolveCombat();
        harness.handleListChoice(player1, choice);
    }
}
