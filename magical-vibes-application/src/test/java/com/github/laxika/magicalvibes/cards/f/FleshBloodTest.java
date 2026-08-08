package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Flesh // Blood is one card whose two halves (and their fusion) are the three modes of a single
 * modal sorcery, each paying its own total cost.
 */
class FleshBloodTest extends BaseCardTest {

    private static final int FLESH = 0;
    private static final int BLOOD = 1;
    private static final int FUSE = 2;

    private void addFleshMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addBloodMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Flesh exiles the graveyard creature and puts that many +1/+1 counters on the target")
    void fleshExilesAndBoosts() {
        Card elemental = new AirElemental();
        harness.setGraveyard(player2, List.of(elemental));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FleshBlood()));
        addFleshMana();

        harness.castModalSorcery(player1, 0, FLESH, List.of(elemental.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()).stream().map(Card::getName).toList())
                .contains("Air Elemental");
    }

    @Test
    @DisplayName("Flesh cannot put its counters on a player")
    void fleshCannotTargetPlayer() {
        Card elemental = new AirElemental();
        harness.setGraveyard(player2, List.of(elemental));

        harness.setHand(player1, List.of(new FleshBlood()));
        addFleshMana();

        UUID elementalId = elemental.getId();
        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, FLESH, List.of(elementalId, playerId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blood makes the chosen creature deal its power to any target")
    void bloodDealsPowerToPlayer() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new FleshBlood()));
        addBloodMana();

        // Declaration order is "any target", then the creature you control.
        harness.castModalSorcery(player1, 0, BLOOD, List.of(player2.getId(), giant.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Blood's damage source must be a creature you control")
    void bloodSourceMustBeControlled() {
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new FleshBlood()));
        addBloodMana();

        UUID playerId = player2.getId();
        UUID giantId = opponentGiant.getId();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, BLOOD, List.of(playerId, giantId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fuse resolves Flesh before Blood, so the counters swell the damage the same creature deals")
    void fuseResolvesFleshThenBlood() {
        Card elemental = new AirElemental();
        harness.setGraveyard(player2, List.of(elemental));
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new FleshBlood()));
        addFleshMana();
        addBloodMana();

        // Declaration order: graveyard card, any target, creature to receive counters, damage source.
        harness.castModalSorcery(player1, 0, FUSE,
                List.of(elemental.getId(), player2.getId(), giant.getId(), giant.getId()));
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Fuse cannot be cast for only one half's mana")
    void fuseRequiresBothHalvesCost() {
        Card elemental = new AirElemental();
        harness.setGraveyard(player2, List.of(elemental));
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new FleshBlood()));
        addFleshMana();

        UUID elementalId = elemental.getId();
        UUID playerId = player2.getId();
        UUID giantId = giant.getId();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, FUSE,
                List.of(elementalId, playerId, giantId, giantId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
