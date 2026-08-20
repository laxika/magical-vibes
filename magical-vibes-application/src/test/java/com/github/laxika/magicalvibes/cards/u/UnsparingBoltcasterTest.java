package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnsparingBoltcasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 5 damage to an opponent's creature dealt damage this turn")
    void etbDealsFiveDamageToDamagedOpponentCreature() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        UUID targetId = target.getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Avatar of Might").getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        UUID targetId = addCreatureReady(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target a damaged creature controlled by its controller")
    void cannotTargetOwnCreature() {
        UUID targetId = addCreatureReady(player1, new GrizzlyBears()).getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("ETB does not trigger when no creature was dealt damage this turn")
    void etbDoesNotTriggerWithoutValidTarget() {
        harness.setHand(player1, List.of(new UnsparingBoltcaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
