package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CidTimelessArtificer;
import com.github.laxika.magicalvibes.cards.e.ElshaThreefoldMaster;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpuzzemStrategist;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheArchimandrite.class, CidTimelessArtificer.class, ElshaThreefoldMaster.class,
        SpuzzemStrategist.class, GrizzlyBears.class})
class TheArchimandriteTest extends BaseCardTest {

    @Test
    @DisplayName("Life gain boosts and grants vigilance to Advisors, Artificers, and Monks")
    void lifeGainBoostsMatchingCreatures() {
        Permanent archimandrite = harness.addToBattlefieldAndReturn(player1, new TheArchimandrite());
        Permanent artificer = harness.addToBattlefieldAndReturn(player1, new CidTimelessArtificer());
        Permanent monk = harness.addToBattlefieldAndReturn(player1, new ElshaThreefoldMaster());
        Permanent advisor = harness.addToBattlefieldAndReturn(player1, new SpuzzemStrategist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setLife(player1, 20);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        assertThat(archimandrite.getPowerModifier()).isEqualTo(3);
        assertThat(artificer.getPowerModifier()).isEqualTo(3);
        assertThat(monk.getPowerModifier()).isEqualTo(3);
        assertThat(advisor.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, archimandrite, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, artificer, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, monk, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, advisor, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(archimandrite.getPowerModifier()).isZero();
        assertThat(artificer.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, archimandrite, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Tapping three matching creatures draws a card")
    void tappingThreeMatchingCreaturesDrawsCard() {
        Permanent archimandrite = harness.addToBattlefieldAndReturn(player1, new TheArchimandrite());
        Permanent artificer = harness.addToBattlefieldAndReturn(player1, new CidTimelessArtificer());
        Permanent monk = harness.addToBattlefieldAndReturn(player1, new ElshaThreefoldMaster());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(archimandrite),
                0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(archimandrite.isTapped()).isTrue();
        assertThat(artificer.isTapped()).isTrue();
        assertThat(monk.isTapped()).isTrue();
    }
}
