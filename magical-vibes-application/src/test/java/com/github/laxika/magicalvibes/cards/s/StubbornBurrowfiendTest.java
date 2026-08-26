package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StubbornBurrowfiend.class, Forest.class, GrizzlyBears.class})
class StubbornBurrowfiendTest extends BaseCardTest {

    @Test
    @DisplayName("Saddling mills two cards, then boosts by creature cards in the graveyard")
    void saddlingMillsAndBoostsByCreatureCardsInGraveyard() {
        Permanent burrowfiend = addCreatureReady(player1, new StubbornBurrowfiend());
        Permanent saddler = addCreatureReady(player1, new GrizzlyBears());
        Card creatureAlreadyInGraveyard = new GrizzlyBears();
        Card milledCreature = new GrizzlyBears();
        Card milledLand = new Forest();
        harness.setGraveyard(player1, List.of(creatureAlreadyInGraveyard));
        harness.setLibrary(player1, List.of(milledCreature, milledLand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(burrowfiend.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, burrowfiend)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, burrowfiend)).isEqualTo(4);
    }

    @Test
    @DisplayName("The saddle trigger fires only once each turn")
    void saddleTriggerFiresOnlyOnceEachTurn() {
        Permanent burrowfiend = addCreatureReady(player1, new StubbornBurrowfiend());
        Permanent firstSaddler = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondSaddler = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstSaddler.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, burrowfiend)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, burrowfiend)).isEqualTo(4);
    }
}
