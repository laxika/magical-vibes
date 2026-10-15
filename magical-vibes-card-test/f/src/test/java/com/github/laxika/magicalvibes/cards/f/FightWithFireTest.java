package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.cards.c.ColdWaterSnapper;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({FightWithFire.class, GrizzlyBears.class, HillGiant.class, Plains.class,
        ChandraNalaar.class, JaceBeleren.class, ImprisonedInTheMoon.class,
        ColdWaterSnapper.class, BlinkOfAnEye.class})
class FightWithFireTest extends BaseCardTest {

    @Test
    void deals5DamageToTargetCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

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

        // The kicked spell may assign all its damage to a player.
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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

        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

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

        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
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

        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(jace.getId());

        assertThatThrownBy(() ->
                harness.castKickedSorcery(player1, 0, Map.of(
                        jace.getId(), 4,
                        player2.getId(), 6
                ))
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    @Test
    void kickedCanTargetPlayerWithoutCreaturesOnBattlefield() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castKickedSorcery(player1, 0, Map.of(player2.getId(), 10));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 10);
        harness.assertInGraveyard(player1, "Fight with Fire");
    }

    @Test
    void unkickedDealsExactlyFiveDamageToOwnCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new ColdWaterSnapper());
        snapper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castSorcery(player1, 0, snapper.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cold-Water Snapper");
        assertThat(snapper.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void unkickedDoesNothingWhenItsOnlyTargetLeaves() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire(), new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.RED, 3);
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new ColdWaterSnapper());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, snapper.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, snapper.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cold-Water Snapper");
        harness.assertInGraveyard(player1, "Fight with Fire");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void kickedCanChooseZeroTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castKickedSorcery(player1, 0, Map.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player1, "Fight with Fire");
    }

    @Test
    void kickedCannotTargetOpponentsHexproofCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);
        Permanent snapper = harness.addToBattlefieldAndReturn(player2, new ColdWaterSnapper());

        assertThatThrownBy(() -> harness.castKickedSorcery(player1, 0,
                Map.of(snapper.getId(), 5, player2.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unkickedCannotTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addToBattlefield(player1, new ColdWaterSnapper());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayKickerWithOnlyBaseMana() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castKickedSorcery(player1, 0,
                Map.of(player2.getId(), 10)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illegalTargetDoesNotRedistributeItsDamage() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire(), new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.RED, 9);
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new ColdWaterSnapper());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castKickedSorcery(player1, 0, Map.of(snapper.getId(), 3, player2.getId(), 7));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, snapper.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Cold-Water Snapper");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 7);
    }
}
