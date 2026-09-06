package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YgraEaterOfAll.class, GrizzlyBears.class})
class YgraEaterOfAllTest extends BaseCardTest {

    @Test
    void otherCreaturesBecomeFoodArtifacts() {
        harness.addToBattlefield(player1, new YgraEaterOfAll());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.isArtifact(gd, ally)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ally, CardSubtype.FOOD)).isTrue();
        assertThat(gqs.isArtifact(gd, opponent)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponent, CardSubtype.FOOD)).isTrue();
    }

    @Test
    void sacrificingAFoodGainsLifeAndPutsCountersOnYgra() {
        Permanent ygra = harness.addToBattlefieldAndReturn(player1, new YgraEaterOfAll());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(ygra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
