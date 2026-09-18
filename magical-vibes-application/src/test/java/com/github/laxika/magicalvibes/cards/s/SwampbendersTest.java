package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Swampbenders.class, Forest.class, Swamp.class})
class SwampbendersTest extends BaseCardTest {

    @Test
    void powerAndToughnessCountSwampsOnTheBattlefield() {
        Permanent swampbenders = harness.addToBattlefieldAndReturn(player1, new Swampbenders());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentSwamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, swampbenders)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swampbenders)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, ownForest, CardSubtype.SWAMP)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponentSwamp, CardSubtype.SWAMP)).isTrue();
    }

    @Test
    void onlyOwnLandsBecomeSwampsAndGainBlackMana() {
        harness.addToBattlefield(player1, new Swampbenders());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(gqs.hasEffectiveSubtype(gd, ownForest, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ownForest, CardSubtype.SWAMP)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponentForest, CardSubtype.SWAMP)).isFalse();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(ownForest.isTapped()).isTrue();
    }
}
