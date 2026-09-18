package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindsDesire.class, ScornfulEgotist.class, TempleOfTheFalseGod.class})
class MindsDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Mind's Desire")
    void stormCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new ScornfulEgotist());
        gd.recordSpellCast(player2.getId(), new ScornfulEgotist());
        castMindsDesire();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Storm creates no copies when Mind's Desire is the first spell of the turn")
    void stormCreatesNoCopiesForFirstSpell() {
        castMindsDesire();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Shuffles, exiles the top card, and lets its controller cast it for free")
    void exilesTopCardForFreeCast() {
        Card topCard = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(topCard));
        castMindsDesire();

        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(topCard.getId());

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("The exiled card may be played as a land")
    void playsExiledLand() {
        Card topCard = new TempleOfTheFalseGod();
        harness.setLibrary(player1, List.of(topCard));
        castMindsDesire();

        resolveAllTriggers();
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Each Storm copy resolves Mind's Desire's library effect")
    void stormCopiesResolveLibraryEffect() {
        Card firstCard = new ScornfulEgotist();
        Card secondCard = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        gd.recordSpellCast(player1.getId(), new ScornfulEgotist());
        castMindsDesire();

        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstCard, secondCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(firstCard.getId(), player1.getId())
                .containsEntry(secondCard.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(firstCard.getId(), secondCard.getId());
    }

    private void castMindsDesire() {
        harness.castFromHand(player1, new MindsDesire(), "{4}{U}{U}");
    }
}
