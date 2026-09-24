package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DelightedHalfling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BilbosRing.class, DelightedHalfling.class, GrizzlyBears.class})
class BilbosRingTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has hexproof and can't be blocked")
    void equippedCreatureHasHexproofAndCantBeBlocked() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent ring = addReady(player1, new BilbosRing());
        ring.setAttachedTo(creature.getId());
        addReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Attacking alone draws a card and costs 1 life")
    void attackingAloneDrawsAndLosesLife() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);

        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent ring = addReady(player1, new BilbosRing());
        ring.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The attack trigger does not resolve when the equipped creature attacks with another creature")
    void attackingWithAnotherCreatureDoesNotTrigger() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);

        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent ring = addReady(player1, new BilbosRing());
        ring.setAttachedTo(creature.getId());
        addReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Equip Halfling attaches the Ring to a Halfling")
    void equipHalflingAttachesToHalfling() {
        Permanent ring = addReady(player1, new BilbosRing());
        Permanent halfling = addReady(player1, new DelightedHalfling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, halfling.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(halfling.getId());
    }

    @Test
    @DisplayName("Equip Halfling rejects a non-Halfling")
    void equipHalflingRejectsNonHalfling() {
        addReady(player1, new BilbosRing());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Halfling");
    }

    @Test
    @DisplayName("Equip {4} attaches the Ring to any creature")
    void genericEquipAttachesToAnyCreature() {
        Permanent ring = addReady(player1, new BilbosRing());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
