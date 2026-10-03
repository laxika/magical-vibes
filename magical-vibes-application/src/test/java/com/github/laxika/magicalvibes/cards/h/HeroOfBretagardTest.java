package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.Expel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindSwords;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroOfBretagard.class, MindSwords.class, Expel.class, GrizzlyBears.class, Forest.class})
class HeroOfBretagardTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter on itself for each card exiled from hand")
    void putsCountersForCardsExiledFromHand() {
        Permanent hero = addCreatureReady(player1, new HeroOfBretagard());
        harness.setHand(player1, List.of(new MindSwords(), new GrizzlyBears(), new Forest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts a counter on itself when a spell exiles a battlefield permanent")
    void putsCounterForExiledPermanent() {
        Permanent hero = addCreatureReady(player1, new HeroOfBretagard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Thresholds grant flying, indestructible, Angel, and God")
    void thresholdsGrantAbilitiesAndSubtypes() {
        Permanent hero = addCreatureReady(player1, new HeroOfBretagard());

        hero.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.ANGEL)
                .doesNotContain(CardSubtype.GOD);

        hero.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.ANGEL, CardSubtype.GOD);
    }
}
