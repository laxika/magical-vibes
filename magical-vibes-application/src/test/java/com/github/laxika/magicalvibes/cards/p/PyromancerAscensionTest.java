package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PyromancerAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("A matching instant or sorcery offers a quest counter")
    void matchingSpellOffersQuestCounter() {
        var ascension = addAscension(0);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell without a same-name card in the graveyard does not add a counter")
    void differentNameDoesNotOfferQuestCounter() {
        var ascension = addAscension(0);
        harness.setGraveyard(player1, List.of(new PyromancerAscension()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Two quest counters allow an instant or sorcery to be copied")
    void twoCountersAllowCopy() {
        var ascension = addAscension(2);
        harness.setGraveyard(player1, List.of(new PyromancerAscension()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(entry -> entry.getCard().getName().equals("Shock")).count())
                .isGreaterThanOrEqualTo(2);
    }

    private com.github.laxika.magicalvibes.model.Permanent addAscension(int counters) {
        var ascension = harness.addToBattlefieldAndReturn(player1, new PyromancerAscension());
        ascension.setCounterCount(CounterType.QUEST, counters);
        return ascension;
    }
}
