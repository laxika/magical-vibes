package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistSyndicateNaga.class, GrizzlyBears.class})
class MistSyndicateNagaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of itself when dealing combat damage to a player")
    void createsTokenCopyOnCombatDamage() {
        Permanent naga = addCreatureReady(player1, new MistSyndicateNaga());
        naga.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mist-Syndicate Naga")).hasSize(2);
    }

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts the Naga in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MistSyndicateNaga()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.activateHandAbility(player1, 0, bears.getId());
            harness.passBothPriorities();
        });

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent naga = findPermanent(player1, "Mist-Syndicate Naga");
        assertThat(naga.isTapped()).isTrue();
        assertThat(naga.isAttacking()).isTrue();
        assertThat(naga.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A token copy enters untapped and outside combat and can create another copy")
    void tokenCopyCanCreateAnotherCopy() {
        Permanent naga = addCreatureReady(player1, new MistSyndicateNaga());
        naga.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Mist-Syndicate Naga").stream()
                .filter(permanent -> !permanent.getId().equals(naga.getId()))
                .findFirst().orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mist-Syndicate Naga")).hasSize(3);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Combat damage to a creature does not create a token copy")
    void blockedNagaDoesNotCreateToken() {
        addCreatureReady(player1, new MistSyndicateNaga());
        addCreatureReady(player2, new MistSyndicateNaga());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mist-Syndicate Naga")).isEmpty();
        assertThat(findPermanents(player2, "Mist-Syndicate Naga")).isEmpty();
        harness.assertInGraveyard(player1, "Mist-Syndicate Naga");
        harness.assertInGraveyard(player2, "Mist-Syndicate Naga");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
