package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoarOfJukaiTest extends BaseCardTest {

    @Test
    @DisplayName("Gives each blocked creature +2/+2 when you control a Forest")
    void boostsBlockedCreaturesWithForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blocked = addAttackingCreature(player1);
        Permanent unblocked = addAttackingCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());

        castRoar();

        assertThat(blocked.getEffectivePower()).isEqualTo(4);
        assertThat(blocked.getEffectiveToughness()).isEqualTo(4);
        assertThat(unblocked.getPowerModifier()).isZero();
        assertThat(blocker.getPowerModifier()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not boost blocked creatures without a Forest")
    void doesNotBoostWithoutForest() {
        Permanent blocked = addAttackingCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());

        castRoar();

        assertThat(blocked.getPowerModifier()).isZero();
        assertThat(blocked.getToughnessModifier()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Splicing applies Roar of Jukai and makes an opponent gain 5 life")
    void splicesForOpponentLifeGain() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blocked = addAttackingCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());
        Card arcaneHost = new HolyDay().createRuntimeCopy();
        arcaneHost.setSubtypes(List.of(CardSubtype.ARCANE));
        harness.setHand(player1, List.of(arcaneHost, new RoarOfJukai()));
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithSplice(player1, 0, null, List.of(1));
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        assertThat(blocked.getEffectivePower()).isEqualTo(4);
        assertThat(blocked.getEffectiveToughness()).isEqualTo(4);
        harness.assertInHand(player1, "Roar of Jukai");
    }

    private void castRoar() {
        harness.setHand(player1, List.of(new RoarOfJukai()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addAttackingCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = addReadyCreature(player);
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
