package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FledglingDragon.class, GrizzlyBears.class})
class FledglingDragonTest extends BaseCardTest {

    @Test
    void remainsBaseSizeBelowThreshold() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertStats(dragon, 2, 2);
    }

    @Test
    void getsThresholdBoostAtSevenCards() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        assertStats(dragon, 5, 5);
    }

    @Test
    void thresholdGrantsRedPumpAbility() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertStats(dragon, 6, 5);
    }

    @Test
    void thresholdEffectsEndWhenGraveyardDropsBelowSeven() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        assertStats(dragon, 5, 5);

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertStats(dragon, 2, 2);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
