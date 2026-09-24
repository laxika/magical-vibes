package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientSpring;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StandDeliver.class, AncientSpring.class, KavuAggressor.class, ScorchingLava.class})
class StandDeliverTest extends BaseCardTest {

    @Test
    @DisplayName("Stand prevents the next 2 damage to target creature")
    void standPreventsNextTwoDamageToTargetCreature() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuAggressor());

        harness.setHand(player1, List.of(new StandDeliver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 0, kavu.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new ScorchingLava(), new ScorchingLava()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, kavu.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kavu);
        assertThat(kavu.getMarkedDamage()).isZero();

        harness.castAndResolveInstant(player2, 0, kavu.getId());

        harness.assertNotOnBattlefield(player1, "Kavu Aggressor");
        harness.assertInGraveyard(player1, "Kavu Aggressor");
    }

    @Test
    @DisplayName("Deliver returns target permanent to its owner's hand")
    void deliverReturnsPermanentToHand() {
        Permanent ancientSpring = harness.addToBattlefieldAndReturn(player2, new AncientSpring());

        harness.setHand(player1, List.of(new StandDeliver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 1, ancientSpring.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ancient Spring");
        harness.assertInHand(player2, "Ancient Spring");
    }

    @Test
    @DisplayName("Stand cannot target a noncreature permanent")
    void standCannotTargetNoncreaturePermanent() {
        Permanent ancientSpring = harness.addToBattlefieldAndReturn(player2, new AncientSpring());

        harness.setHand(player1, List.of(new StandDeliver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, ancientSpring.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deliver cannot target a player")
    void deliverCannotTargetPlayer() {
        harness.setHand(player1, List.of(new StandDeliver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
