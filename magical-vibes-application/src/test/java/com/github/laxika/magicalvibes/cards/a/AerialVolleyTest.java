package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialVolley.class, AirElemental.class, SuntailHawk.class, Ornithopter.class, SoulWarden.class})
class AerialVolleyTest extends BaseCardTest {

    @Test
    void deals3DamageToSingleFlyer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.castInstant(player1, 0, Map.of(target.getId(), 3));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    void dividesDamageAmongThreeFlyers() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.castInstant(player1, 0, Map.of(
                hawk.getId(), 1,
                thopter.getId(), 1,
                elemental.getId(), 1
        ));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(hawk.getId()))
                .anyMatch(p -> p.getId().equals(thopter.getId()))
                .anyMatch(p -> p.getId().equals(elemental.getId()));
        assertThat(elemental.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotTargetCreatureWithoutFlying() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent ground = harness.addToBattlefieldAndReturn(player2, new SoulWarden());

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(ground.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustSumTo3() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(target.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void skipsTargetThatGainsHexproofBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent protectedTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        harness.castInstant(player1, 0,
                Map.of(protectedTarget.getId(), 2, legalTarget.getId(), 1));
        protectedTarget.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(protectedTarget.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(legalTarget.getId()));
    }

    @Test
    void dividesDamageBetweenOwnAndOpposingFlyers() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.castInstant(player1, 0, Map.of(own.getId(), 1, opposing.getId(), 2));
        harness.passBothPriorities();

        assertThat(own.getMarkedDamage()).isEqualTo(1);
        assertThat(opposing.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotAssignZeroDamageToATarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(first.getId(), 3, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRedistributeDamageWhenATargetLosesFlying() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent illegal = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent legal = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.castInstant(player1, 0, Map.of(illegal.getId(), 2, legal.getId(), 1));
        illegal.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        assertThat(illegal.getMarkedDamage()).isZero();
        assertThat(legal.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotDealDamageWhenAllTargetsBecomeIllegal() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.castInstant(player1, 0, Map.of(target.getId(), 3));
        target.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Aerial Volley");
    }
}
