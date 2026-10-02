package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightwingBandit.class, Divination.class, Island.class, Shock.class})
class BlightwingBanditTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell during an opponent's turn exiles the top card face down")
    void firstSpellDuringOpponentsTurnExilesTopCardFaceDown() {
        harness.addToBattlefield(player1, new BlightwingBandit());
        Card topCard = new Divination();
        harness.setLibrary(player1, List.of(topCard, new Island()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(entry.exilerId()).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
    }

    @Test
    @DisplayName("Only the first spell during an opponent's turn triggers")
    void onlyFirstSpellDuringOpponentsTurnTriggers() {
        harness.addToBattlefield(player1, new BlightwingBandit());
        Card firstTopCard = new Island();
        harness.setLibrary(player1, List.of(firstTopCard, new Island()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        int librarySizeAfterFirstSpell = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeAfterFirstSpell);
    }

    @Test
    @DisplayName("The ability does not trigger during its controller's turn")
    void doesNotTriggerDuringOwnTurn() {
        harness.addToBattlefield(player1, new BlightwingBandit());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The controller can cast the exiled spell using mana of any type")
    void castsExiledSpellWithAnyManaType() {
        harness.addToBattlefield(player1, new BlightwingBandit());
        Card topCard = new Divination();
        harness.setLibrary(player1, List.of(topCard, new Island(), new Island()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }
}
