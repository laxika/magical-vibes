package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimeTender.class, GrizzlyBears.class})
class RimeTenderTest extends BaseCardTest {

    @Test
    void untapsAnotherTargetSnowPermanent() {
        Permanent tender = addCreatureReady(player1, new RimeTender());
        Permanent target = addSnowPermanent(player2);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(tender.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetNonsnowPermanent() {
        addCreatureReady(player1, new RimeTender());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetItself() {
        Permanent tender = addCreatureReady(player1, new RimeTender());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tender.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSnowPermanent(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        TestCards.mutableCard(permanent).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
