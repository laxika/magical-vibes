package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHuntsmansRedemption.class, Forest.class, GrizzlyBears.class, Opt.class})
class TheHuntsmansRedemptionTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a 3/3 green Beast token")
    void chapterICreatesBeastToken() {
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        List<Permanent> beasts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(1);
        assertThat(beasts.getFirst().getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.GREEN);
        assertThat(beasts.getFirst().getEffectivePower()).isEqualTo(3);
        assertThat(beasts.getFirst().getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter II may sacrifice a creature and search for a creature or basic land")
    void chapterIISacrificesAndSearchesCreatureOrBasicLand() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSaga(1);
        Card invalid = new Opt();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(invalid, land, creature));

        triggerChapter();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(sacrificed.getId());
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(land, creature);
        assertThat(search.params().reveals()).isTrue();
        harness.handleCardChosen(player1, search.params().cards().indexOf(land));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("Chapter II can be declined without sacrificing or searching")
    void chapterIICanBeDeclined() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSaga(1);
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
    }

    @Test
    @DisplayName("Chapter III boosts up to two target creatures and gives them trample")
    void chapterIIIBoostsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSaga(2);

        triggerChapter();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(first.getId(), second.getId(), third.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, third, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheHuntsmansRedemption());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
