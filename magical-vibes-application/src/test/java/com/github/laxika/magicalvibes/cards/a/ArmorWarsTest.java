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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Accepting chapter I with no artifacts still makes the opponent draw")
    void acceptingChapterIWithNoArtifactsMakesOpponentDraw() {
        addSagaWithLore(0);
        int playerDeckBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();

        triggerNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(playerDeckBefore);
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore - 1);
    }

    @Test
    @DisplayName("Chapter I counts controlled artifacts at resolution and excludes opposing artifacts")
    void chapterICountsArtifactsAtResolution() {
        addSagaWithLore(0);
        harness.addToBattlefield(player2, new JhoirasFamiliar());
        int playerDeckBefore = gd.playerDecks.get(player1.getId()).size();

        triggerNextChapter();
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(playerDeckBefore - 1);
    }

    @Test
    @DisplayName("Chapter II discounts multiple artifacts even after the Saga leaves")
    void chapterIIDiscountPersistsWithoutSagaAndAppliesToMultipleSpells() {
        Permanent saga = addSagaWithLore(1);
        harness.setHand(player1, List.of(new ChromaticStar(), new ChromaticStar()));

        triggerNextChapter();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof ChromaticStar)
                .hasSize(2);
    }

    @Test
    @DisplayName("Chapter II's discount expires when the turn ends")
    void chapterIIDiscountExpiresAtEndOfTurn() {
        Permanent saga = addSagaWithLore(1);

        triggerNextChapter();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChromaticStar()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chapter III deals no damage without controlled artifacts")
    void chapterIIIDealsNoDamageWithoutControlledArtifacts() {
        Permanent saga = addSagaWithLore(2);
        harness.addToBattlefield(player2, new JhoirasFamiliar());

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    @DisplayName("Chapter III uses the greatest artifact mana value at resolution")
    void chapterIIIUsesManaValueAtResolution() {
        addSagaWithLore(2);
        harness.addToBattlefield(player1, new ChromaticStar());
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(familiar);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
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
