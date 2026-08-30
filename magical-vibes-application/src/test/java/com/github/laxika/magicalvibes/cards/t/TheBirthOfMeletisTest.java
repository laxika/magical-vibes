package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBirthOfMeletis.class, Forest.class, GrizzlyBears.class, Plains.class})
class TheBirthOfMeletisTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I searches for a basic Plains and puts it into hand")
    void chapterISearchesForBasicPlains() {
        Permanent saga = addSagaWithLore(0);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Plains()));

        advanceToNextChapter();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).singleElement()
                .satisfies(card -> assertThat(card.getSubtypes()).contains(CardSubtype.PLAINS));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getSubtypes().contains(CardSubtype.PLAINS));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("Chapter II creates a 0/4 colorless Wall artifact creature token with defender")
    void chapterIICreatesWallToken() {
        Permanent saga = addSagaWithLore(1);

        advanceToNextChapter();

        Permanent wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != saga && permanent.getCard().getName().equals("Wall"))
                .findFirst()
                .orElseThrow();
        assertThat(wall.getCard().getPower()).isZero();
        assertThat(wall.getCard().getToughness()).isEqualTo(4);
        assertThat(wall.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(wall.getCard().getSubtypes()).contains(CardSubtype.WALL);
        assertThat(wall.getCard().getKeywords()).contains(Keyword.DEFENDER);
    }

    @Test
    @DisplayName("Chapter III gains 2 life")
    void chapterIIIGainsLife() {
        Permanent saga = addSagaWithLore(2);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToNextChapter();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBirthOfMeletis());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
