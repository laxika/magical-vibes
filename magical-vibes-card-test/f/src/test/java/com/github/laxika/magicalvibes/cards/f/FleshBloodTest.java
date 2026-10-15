package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.b.BoneyardWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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
@CardUsed({FleshBlood.class, AirElemental.class, GrizzlyBears.class, HillGiant.class,
        BoneyardWurm.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
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

    @Test
    @DisplayName("Flesh uses a characteristic-defining power before the creature leaves its graveyard")
    void fleshUsesGraveyardPowerBeforeExiling() {
        Card wurm = new BoneyardWurm();
        Card elemental = new AirElemental();
        harness.setGraveyard(player2, List.of(wurm, elemental));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FleshBlood()));
        addFleshMana();

        harness.castModalSorcery(player1, 0, FLESH, List.of(wurm.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(elemental);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(wurm);
    }

    @Test
    @DisplayName("Blood can damage a battle")
    void bloodCanTargetBattle() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        harness.setHand(player1, List.of(new FleshBlood()));
        addBloodMana();

        harness.castModalSorcery(player1, 0, BLOOD, List.of(battle.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flesh still exiles when the creature receiving counters leaves the battlefield")
    void fleshExilesWhenCounterRecipientIsGone() {
        Card elemental = new AirElemental();
        harness.setGraveyard(player2, List.of(elemental));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FleshBlood()));
        addFleshMana();
        harness.castModalSorcery(player1, 0, FLESH, List.of(elemental.getId(), bears.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(elemental);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A fused spell still deals damage if its graveyard target leaves before resolution")
    void fuseStillDealsDamageWhenGraveyardTargetIsGone() {
        Card elemental = new AirElemental();
        harness.setGraveyard(player2, List.of(elemental));
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new FleshBlood()));
        addFleshMana();
        addBloodMana();
        harness.castModalSorcery(player1, 0, FUSE,
                List.of(elemental.getId(), player2.getId(), giant.getId(), giant.getId()));

        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Blood deals no damage when its source creature leaves before resolution")
    void bloodDoesNotUseLastKnownPowerOfMissingTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new FleshBlood()));
        addBloodMana();
        harness.castModalSorcery(player1, 0, BLOOD, List.of(player2.getId(), giant.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(giant);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blood uses the source creature's current power and can target that same creature")
    void bloodCanDamageItsSourceUsingCurrentPower() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new FleshBlood()));
        addBloodMana();
        harness.castModalSorcery(player1, 0, BLOOD, List.of(giant.getId(), giant.getId()));

        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Flesh can exile a creature from your own graveyard and boost an opposing creature")
    void fleshCanBoostOpposingCreatureFromOwnGraveyard() {
        Card elemental = new AirElemental();
        harness.setGraveyard(player1, List.of(elemental));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FleshBlood()));
        addFleshMana();

        harness.castModalSorcery(player1, 0, FLESH, List.of(elemental.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elemental);
    }
}
