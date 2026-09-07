package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NorthWindAvatar.class, GrizzlyBears.class, Zombify.class})
class NorthWindAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, may put a card from outside the game into hand")
    void castMayPutOutsideTheGameCardIntoHand() {
        Card chosen = new GrizzlyBears();
        setSideboard(chosen);

        castAvatar();

        PendingInteraction.LibrarySearch search = pendingSearch();
        assertThat(search.params().cards()).containsExactly(chosen);
        choose(chosen);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May decline to put a card from outside the game into hand")
    void mayDeclineOutsideTheGameCard() {
        Card available = new GrizzlyBears();
        setSideboard(available);

        castAvatar();
        choose(null);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(available);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(available);
    }

    @Test
    @DisplayName("Does not trigger when it enters the battlefield without being cast")
    void doesNotTriggerWhenNotCast() {
        NorthWindAvatar avatar = new NorthWindAvatar();
        Card available = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(avatar));
        setSideboard(available);
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, avatar.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(available);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(avatar.getId()));
    }

    private void castAvatar() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NorthWindAvatar()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setSideboard(Card... cards) {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(cards)));
    }

    private PendingInteraction.LibrarySearch pendingSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void choose(Card card) {
        PendingInteraction.LibrarySearch search = pendingSearch();
        int index = card == null ? -1 : search.params().cards().indexOf(card);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
