package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.s.Skulltap;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindsDesire.class, ScornfulEgotist.class, Skulltap.class, TempleOfTheFalseGod.class})
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

    @Test
    @DisplayName("An empty library causes no exile and does not cause a failed draw")
    void emptyLibraryResolvesNormally() {
        harness.setLibrary(player1, List.of());
        castMindsDesire();

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mind's Desire");
    }

    @Test
    @DisplayName("A creature exiled by a Storm copy cannot be cast while the original remains on the stack")
    void freeCastStillRequiresNormalTiming() {
        Card topCard = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(topCard));
        gd.recordSpellCast(player1.getId(), new ScornfulEgotist());
        castMindsDesire();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        resolveAllTriggers();
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Scornful Egotist");
    }

    @Test
    @DisplayName("Playing an exiled land still uses the normal land allowance")
    void cannotPlayLandAfterLandAllowanceIsUsed() {
        Card topCard = new TempleOfTheFalseGod();
        harness.setLibrary(player1, List.of(topCard));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        castMindsDesire();
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.assertNotOnBattlefield(player1, "Temple of the False God");
    }

    @Test
    @DisplayName("An unplayed card remains exiled but its permission expires at turn end")
    void playPermissionExpiresAtTurnEnd() {
        Card topCard = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(topCard));
        castMindsDesire();
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the controller may play the exiled card")
    void opponentCannotUsePlayPermission() {
        Card topCard = new TempleOfTheFalseGod();
        harness.setLibrary(player1, List.of(topCard));
        castMindsDesire();
        resolveAllTriggers();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromExile(player2, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.assertNotOnBattlefield(player2, "Temple of the False God");
    }

    @Test
    @DisplayName("A free spell can be cast by paying its required creature sacrifice")
    void castsExiledSpellWithRequiredSacrifice() {
        Card topCard = new Skulltap();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ScornfulEgotist());
        harness.setLibrary(player1, List.of(topCard));
        castMindsDesire();
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new ScornfulEgotist(), new TempleOfTheFalseGod()));

        assertThatCode(() -> harness.castFromExile(player1, topCard.getId())).doesNotThrowAnyException();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, sacrifice.getId());
        }
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Scornful Egotist");
        harness.assertInGraveyard(player1, "Scornful Egotist");
        harness.assertInGraveyard(player1, "Skulltap");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
