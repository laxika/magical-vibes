package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamiteCenserBearer.class, GrizzlyBears.class, ProdigalPyromancer.class})
class SamiteCenserBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it prevents the next damage to each creature you control")
    void sacrificesAndPreventsDamageToControlledCreatures() {
        addReady(player1, new SamiteCenserBearer());
        Permanent ownCreature = addReady(player1, new GrizzlyBears());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Samite Censer-Bearer");
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevention does not affect an opponent's creature or creatures entering later")
    void onlyProtectsControlledCreaturesAtResolution() {
        addReady(player1, new SamiteCenserBearer());
        Permanent ownCreature = addReady(player1, new GrizzlyBears());
        Permanent firstPyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent secondPyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent thirdPyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent opponentCreature = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, firstPyromancer), null, ownCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondPyromancer), null, opponentCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, thirdPyromancer), null, lateCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(lateCreature.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
