package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildfireWickerfolk.class, GrizzlyBears.class, Forest.class, Shock.class, Pacifism.class})
class WildfireWickerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Remains a 3/2 without delirium and has no trample")
    void noDelirium() {
        Permanent wickerfolk = addWickerfolk();

        assertStats(wickerfolk, 3, 2);
        assertThat(gqs.hasKeyword(gd, wickerfolk, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and trample with four distinct card types in its controller's graveyard")
    void deliriumGrantsBoostAndTrample() {
        Permanent wickerfolk = addWickerfolk();
        harness.setGraveyard(player1, fourCardTypes());

        assertStats(wickerfolk, 4, 3);
        assertThat(gqs.hasKeyword(gd, wickerfolk, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Counts distinct card types rather than cards")
    void duplicateCardTypesDoNotReachDelirium() {
        Permanent wickerfolk = addWickerfolk();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Shock()));

        assertStats(wickerfolk, 3, 2);
        assertThat(gqs.hasKeyword(gd, wickerfolk, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses the boost and trample when the graveyard drops below four card types")
    void losesDelirium() {
        Permanent wickerfolk = addWickerfolk();
        harness.setGraveyard(player1, fourCardTypes());
        assertStats(wickerfolk, 4, 3);
        assertThat(gqs.hasKeyword(gd, wickerfolk, Keyword.TRAMPLE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertStats(wickerfolk, 3, 2);
        assertThat(gqs.hasKeyword(gd, wickerfolk, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addWickerfolk() {
        return harness.addToBattlefieldAndReturn(player1, new WildfireWickerfolk());
    }

    private List<com.github.laxika.magicalvibes.model.Card> fourCardTypes() {
        return List.of(new GrizzlyBears(), new Forest(), new Shock(), new Pacifism());
    }

    private void assertStats(Permanent wickerfolk, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, wickerfolk)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, wickerfolk)).isEqualTo(toughness);
    }
}
