package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfWretchedSorrow.class, AirElemental.class, ColossalDreadmaw.class,
        DoomBlade.class, HillGiant.class, MindStone.class})
class MarchOfWretchedSorrowTest extends BaseCardTest {

    @Test
    void dealsXDamageAndGainsXLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new MarchOfWretchedSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(target.getId()), List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    void exilingBlackCardsReducesGenericCostForEachCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new MarchOfWretchedSorrow(), new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantForXWithDiscards(player1, 0, 4, List.of(target.getId()), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactlyInAnyOrder("Doom Blade", "Doom Blade");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void handExileCostOnlyAcceptsBlackCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MarchOfWretchedSorrow(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(target.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new MarchOfWretchedSorrow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(target.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
