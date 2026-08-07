package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AerialVolleyTest extends BaseCardTest {

    @Test
    void deals3DamageToSingleFlyer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = addToBattlefield(player2, new AirElemental());

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

        Permanent hawk = addToBattlefield(player2, new SuntailHawk());
        Permanent thopter = addToBattlefield(player2, new Ornithopter());
        Permanent elemental = addToBattlefield(player2, new AirElemental());

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

        Permanent ground = addToBattlefield(player2, new SoulWarden());

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(ground.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustSumTo3() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = addToBattlefield(player2, new AirElemental());

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(target.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void skipsTargetThatGainsHexproofBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AerialVolley()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent protectedTarget = addToBattlefield(player2, new AirElemental());
        Permanent legalTarget = addToBattlefield(player2, new SuntailHawk());

        harness.castInstant(player1, 0,
                Map.of(protectedTarget.getId(), 2, legalTarget.getId(), 1));
        protectedTarget.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(protectedTarget.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(legalTarget.getId()));
    }

    private Permanent addToBattlefield(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
