package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauthiVoidwalker.class, GrizzlyBears.class, Shock.class})
class DauthiVoidwalkerTest extends BaseCardTest {

    @Test
    void exilesOpponentCardsFromAnywhereWithVoidCounters() {
        harness.addToBattlefield(player1, new DauthiVoidwalker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);

        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        Card exiled = opponentCreature.getCard();
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(gd.exiledCardsWithVoidCounters).containsExactly(exiled.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(exiled);
    }

    @Test
    void choosesOnlyOpponentOwnedVoidCounterCardAndMayPlayItForFreeThisTurn() {
        harness.addToBattlefield(player1, new DauthiVoidwalker());
        Card opponentCard = new GrizzlyBears();
        Card ownCard = new Shock();
        harness.setExile(player2, List.of(opponentCard));
        harness.setExile(player1, List.of(ownCard));
        gd.exiledCardsWithVoidCounters.add(opponentCard.getId());
        gd.exiledCardsWithVoidCounters.add(ownCard.getId());

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ExiledCardMayPlayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentCard.getId());
        assertThat(choice.withoutPayingManaCost()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(opponentCard.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(opponentCard.getId());

        harness.castFromExile(player1, opponentCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentCard);
        assertThat(gd.findExiledCard(opponentCard.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(opponentCard.getId());
    }
}
