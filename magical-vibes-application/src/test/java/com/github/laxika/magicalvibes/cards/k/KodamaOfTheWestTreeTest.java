package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KodamaOfTheWestTree.class, GrizzlyBears.class, Forest.class})
class KodamaOfTheWestTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Modified creatures you control have trample")
    void modifiedCreaturesHaveTrample() {
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent modifiedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unmodifiedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, modifiedBears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodifiedBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A modified creature dealing combat damage searches for a basic land tapped")
    void modifiedCreatureCombatDamageSearchesForBasicLand() {
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        setLibrary(List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement()
                .satisfies(card -> assertThat(card.getName()).isEqualTo("Forest"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("An unmodified creature dealing combat damage does not search")
    void unmodifiedCreatureCombatDamageDoesNotSearch() {
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        setLibrary(List.of(new Forest()));

        declareAttackers(List.of(1));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
