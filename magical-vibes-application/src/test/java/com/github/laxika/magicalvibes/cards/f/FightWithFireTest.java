package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FightWithFireTest extends BaseCardTest {

    @Test
    void deals5DamageToTargetCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // GrizzlyBears is 2/2, 5 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void unkickedGoesToGraveyardAfterResolving() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fight with Fire");
    }

    @Test
    void kickedDeals10DamageDividedAmongCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        // Base cost {2}{R} + kicker {5}{R} = 9 mana total
        harness.addMana(player1, ManaColor.RED, 9);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = addToBattlefield(player2, new HillGiant());

        harness.castKickedSorcery(player1, 0, Map.of(
                bears.getId(), 4,
                giant.getId(), 6
        ));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // GrizzlyBears is 2/2, 4 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        // HillGiant is 3/3, 6 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
    }

    @Test
    void kickedCanDealAllDamageToPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        // Base cost {2}{R} + kicker {5}{R} = 9 mana total
        harness.addMana(player1, ManaColor.RED, 9);

        // A creature must exist so the base spell is considered playable
        addToBattlefield(player2, new GrizzlyBears());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castKickedSorcery(player1, 0, Map.of(
                player2.getId(), 10
        ));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 10);
    }

    @Test
    void kickedCanSplitDamageAmongCreaturesAndPlayers() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        // Base cost {2}{R} + kicker {5}{R} = 9 mana total
        harness.addMana(player1, ManaColor.RED, 9);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castKickedSorcery(player1, 0, Map.of(
                bears.getId(), 3,
                player2.getId(), 7
        ));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Bears is 2/2, 3 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 7);
    }

    @Test
    void kickedDamageAssignmentsMustSumTo10() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        // Only assigning 5 damage — should fail
        assertThatThrownBy(() ->
                harness.castKickedSorcery(player1, 0, Map.of(bears.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedDamageAssignmentsMustBePositive() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.castKickedSorcery(player1, 0, Map.of(
                        bears.getId(), 0,
                        player2.getId(), 10
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedDamageAssignmentsRejectLandTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);

        addToBattlefield(player2, new GrizzlyBears());
        Permanent plains = addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.p.Plains());

        assertThatThrownBy(() ->
                harness.castKickedSorcery(player1, 0, Map.of(
                        plains.getId(), 4,
                        player2.getId(), 6
                ))
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(plains.getId()));
    }

    @Test
    void kickedDamageAssignmentsAcceptAPlaneswalker() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);

        addToBattlefield(player2, new GrizzlyBears());
        Permanent chandra = addToBattlefield(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        harness.castKickedSorcery(player1, 0, Map.of(
                chandra.getId(), 4,
                player2.getId(), 6
        ));
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    /**
     * Each announced assignment target (CR 601.2d) is judged against what the effect declares
     * rather than against a re-implemented type pair, so the cast-time gate reads the planeswalker
     * type after layer 4 (CR 613.1d): a planeswalker Imprisoned in the Moon turned into a colorless
     * land is no longer an any target (CR 115.4).
     */
    @Test
    void kickedDamageAssignmentsRejectAPlaneswalkerLayerFourUnmade() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);

        addToBattlefield(player2, new GrizzlyBears());
        Permanent jace = addToBattlefield(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(jace.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThatThrownBy(() ->
                harness.castKickedSorcery(player1, 0, Map.of(
                        jace.getId(), 4,
                        player2.getId(), 6
                ))
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    private Permanent addToBattlefield(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
