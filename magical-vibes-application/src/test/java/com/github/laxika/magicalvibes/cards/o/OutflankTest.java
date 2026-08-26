package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Outflank.class, AirElemental.class, GrizzlyBears.class})
class OutflankTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of creatures its controller controls")
    void dealsDamageEqualToControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = addAttacker(new AirElemental());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals damage to a blocking creature")
    void dealsDamageToBlockingCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = addBlocker(new AirElemental());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private void castAndResolve(Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Outflank()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addAttacker(Card card) {
        harness.addToBattlefield(player2, card);
        Permanent target = findPermanent(player2, "Air Elemental");
        target.setSummoningSick(false);
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        return target;
    }

    private Permanent addBlocker(Card card) {
        harness.addToBattlefield(player2, card);
        Permanent target = findPermanent(player2, "Air Elemental");
        target.setSummoningSick(false);
        target.setBlocking(true);
        target.addBlockingTargetId(player1.getId());
        return target;
    }
}
