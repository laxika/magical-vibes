package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.q.QuirionElves;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarredPuma.class, GoblinSpy.class, RavenousRats.class, QuirionElves.class, Humble.class})
class ScarredPumaTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without a black or green creature also attacking")
    void cannotAttackWithoutMatchingCreature() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        Permanent goblin = addCreatureReady(player1, new GoblinSpy());
        addOpponentCreature();

        assertThatThrownBy(() -> declareAttackers(List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green creature");
        assertThat(puma.isAttacking()).isFalse();
        assertThat(goblin.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack with a black creature")
    void canAttackWithBlackCreature() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        Permanent blackCreature = addCreatureReady(player1, new RavenousRats());
        addOpponentCreature();

        declareAttackers(List.of(0, 1));

        assertThat(puma.isAttacking()).isTrue();
        assertThat(blackCreature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack when a black creature stays back")
    void cannotAttackWhenMatchingCreatureStaysBack() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        Permanent blackCreature = addCreatureReady(player1, new RavenousRats());
        addOpponentCreature();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green creature");
        assertThat(puma.isAttacking()).isFalse();
        assertThat(blackCreature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack with a green creature")
    void canAttackWithGreenCreature() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        Permanent greenCreature = addCreatureReady(player1, new QuirionElves());
        addOpponentCreature();

        declareAttackers(List.of(0, 1));

        assertThat(puma.isAttacking()).isTrue();
        assertThat(greenCreature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack alone even when the opponent controls a black creature")
    void cannotAttackAloneWithOpposingBlackCreature() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        addCreatureReady(player2, new RavenousRats());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green creature");
        assertThat(puma.isAttacking()).isFalse();
        assertThat(puma.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two red Pumas cannot satisfy each other's attack restriction")
    void twoPumasCannotAttackWithoutMatchingCreature() {
        Permanent firstPuma = addCreatureReady(player1, new ScarredPuma());
        Permanent secondPuma = addCreatureReady(player1, new ScarredPuma());
        addOpponentCreature();

        assertThatThrownBy(() -> declareAttackers(List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green creature");
        assertThat(firstPuma.isAttacking()).isFalse();
        assertThat(secondPuma.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("One black attacker can satisfy the restriction for multiple Pumas")
    void multiplePumasCanAttackWithOneBlackCreature() {
        Permanent firstPuma = addCreatureReady(player1, new ScarredPuma());
        Permanent secondPuma = addCreatureReady(player1, new ScarredPuma());
        Permanent blackCreature = addCreatureReady(player1, new RavenousRats());
        addOpponentCreature();

        declareAttackers(List.of(0, 1, 2));

        assertThat(firstPuma.isAttacking()).isTrue();
        assertThat(secondPuma.isAttacking()).isTrue();
        assertThat(blackCreature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can block alone without a black or green creature")
    void canBlockAlone() {
        addCreatureReady(player1, new GoblinSpy());
        addCreatureReady(player2, new ScarredPuma());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Goblin Spy");
        harness.assertInGraveyard(player2, "Scarred Puma");
    }

    @Test
    @DisplayName("Can attack alone after losing all abilities to Humble")
    void canAttackAloneAfterLosingAbilities() {
        Permanent puma = addCreatureReady(player1, new ScarredPuma());
        addOpponentCreature();
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, puma.getId());
        declareAttackers(List.of(0));

        assertThat(puma.isAttacking()).isTrue();
    }

    private void addOpponentCreature() {
        harness.addToBattlefield(player2, new GoblinSpy());
    }
}
