package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmorWars.class, ChromaticStar.class, JhoirasFamiliar.class})
class ArmorWarsTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I draws for controlled artifacts and makes each opponent draw")
    void chapterIDrawsForArtifactsAndMakesOpponentsDraw() {
        addSagaWithLore(0);
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        harness.setHand(player1, List.of());

        int playerDeckBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();

        triggerNextChapter();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(playerDeckBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore - 1);
    }

    @Test
    @DisplayName("Declining chapter I prevents all of its draws")
    void decliningChapterIDoesNotDraw() {
        addSagaWithLore(0);
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.setHand(player1, List.of());

        int playerDeckBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();

        triggerNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(playerDeckBefore);
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore);
    }

    @Test
    @DisplayName("Chapter II reduces artifact spells by one this turn")
    void chapterIIReducesArtifactSpells() {
        addSagaWithLore(1);
        harness.setHand(player1, List.of(new ChromaticStar()));

        triggerNextChapter();
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chromatic Star");
    }

    @Test
    @DisplayName("Chapter III deals damage equal to the greatest controlled artifact mana value")
    void chapterIIIDealsGreatestArtifactManaValueDamage() {
        Permanent saga = addSagaWithLore(2);
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addToBattlefield(player1, new JhoirasFamiliar());

        triggerNextChapter();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ArmorWars());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
