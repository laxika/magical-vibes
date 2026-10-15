package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
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

@CardUsed({WrapInFlames.class, NestInvader.class, PropheticPrism.class, Vendetta.class})
class WrapInFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to and stops up to three target creatures from blocking")
    void damagesAndStopsThreeCreaturesFromBlocking() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent creature3 = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(creature1.getId(), creature2.getId(), creature3.getId()));

        assertThat(creature1.getMarkedDamage()).isEqualTo(1);
        assertThat(creature2.getMarkedDamage()).isEqualTo(1);
        assertThat(creature3.getMarkedDamage()).isEqualTo(1);
        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
        assertThat(creature3.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target fewer than three creatures")
    void canTargetFewerCreatures() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(creature1.getId(), creature2.getId()));

        assertThat(creature1.getMarkedDamage()).isEqualTo(1);
        assertThat(creature2.getMarkedDamage()).isEqualTo(1);
        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID prismId = prism.getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(prismId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can resolve with no targets even when no creatures exist")
    void canChooseZeroTargets() {
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, List.<UUID>of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wrap in Flames");
    }

    @Test
    @DisplayName("Can target a friendly creature and leaves unchosen creatures unaffected")
    void canTargetOneFriendlyCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(chosen.getId()));

        assertThat(chosen.getMarkedDamage()).isEqualTo(1);
        assertThat(chosen.isCantBlockThisTurn()).isTrue();
        assertThat(unchosen.getMarkedDamage()).isZero();
        assertThat(unchosen.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Preventing the damage does not prevent the blocking restriction")
    void preventedDamageStillStopsBlocking() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 4);
        gd.preventAllDamageToAllCreatures = true;

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A removed target does not stop the remaining legal target from being affected")
    void resolvesForRemainingLegalTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new WrapInFlames(), new Vendetta()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        harness.castAndResolveInstant(player1, 0, removed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nest Invader");
        assertThat(removed.isCantBlockThisTurn()).isFalse();
        assertThat(remaining.getMarkedDamage()).isEqualTo(1);
        assertThat(remaining.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The blocking restriction and marked damage expire at cleanup")
    void blockingRestrictionExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));
        assertThat(creature.isCantBlockThisTurn()).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Rejects more than three targets")
    void rejectsFourTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects choosing the same creature more than once")
    void rejectsDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new WrapInFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Does not affect other creatures when its only target is removed")
    void allTargetsRemoved() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new WrapInFlames(), new Vendetta()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, List.of(removed.getId()));
        harness.castAndResolveInstant(player1, 0, removed.getId());
        harness.passBothPriorities();

        assertThat(unchosen.getMarkedDamage()).isZero();
        assertThat(unchosen.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wrap in Flames");
        harness.assertInGraveyard(player2, "Nest Invader");
    }
}
