package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartOfKiranTest extends BaseCardTest {

    @Test
    void crewsByTappingCreaturesWithTotalPowerAtLeastThree() {
        Permanent heart = addReadyHeart(player1);
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, heart)).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    @Test
    void mayRemoveLoyaltyCounterFromAPlaneswalkerInsteadOfPayingCrew() {
        Permanent heart = addReadyHeart(player1);
        Permanent planeswalker = addPlaneswalker(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, heart)).isTrue();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(planeswalker.isTapped()).isFalse();
    }

    @Test
    void alternatePaymentCannotRemoveALoyaltyCounterFromANonPlaneswalker() {
        addReadyHeart(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        artifact.setCounterCount(CounterType.LOYALTY, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent you control has a counter to remove");
    }

    private Permanent addReadyHeart(Player player) {
        Permanent heart = new Permanent(new HeartOfKiran());
        heart.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(heart);
        return heart;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = new Permanent(new ChandraNalaar());
        planeswalker.setSummoningSick(false);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
