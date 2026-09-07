package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Carom.class, GrizzlyBears.class, Island.class, ProdigalPyromancer.class})
class CaromTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects the next damage to another creature and draws a card")
    void redirectsNextDamageAndDraws() {
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent protectedCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent destination = addReadyCreature(player2, new GrizzlyBears());
        Permanent pyromancer = addReadyCreature(player2, new ProdigalPyromancer());

        castCarom(protectedCreature.getId(), destination.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);

        activatePing(pyromancer, protectedCreature.getId());

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Redirects only one damage")
    void redirectsOnlyOneDamage() {
        Permanent protectedCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent destination = addReadyCreature(player2, new GrizzlyBears());
        Permanent firstPyromancer = addReadyCreature(player2, new ProdigalPyromancer());
        Permanent secondPyromancer = addReadyCreature(player2, new ProdigalPyromancer());

        castCarom(protectedCreature.getId(), destination.getId());

        activatePing(firstPyromancer, protectedCreature.getId());
        activatePing(secondPyromancer, protectedCreature.getId());

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Carom()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot target the same creature twice")
    void cannotUseTheSameTargetTwice() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Carom()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    private void castCarom(java.util.UUID protectedId, java.util.UUID destinationId) {
        harness.setHand(player1, List.of(new Carom()));
        addMana();
        harness.castInstant(player1, 0, List.of(protectedId, destinationId));
        harness.passBothPriorities();
    }

    private void activatePing(Permanent pyromancer, java.util.UUID targetId) {
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer),
                null, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
