package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuthorOfShadows.class, Forest.class, GrizzlyBears.class})
class AuthorOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles opponents' graveyards and chooses one nonland card to cast")
    void exilesGraveyardsAndChoosesOneNonlandCard() {
        GrizzlyBears chosen = new GrizzlyBears();
        GrizzlyBears notChosen = new GrizzlyBears();
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(chosen, notChosen, land));
        Permanent author = castAuthor();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(author.getId()))
                .containsExactlyInAnyOrder(chosen, notChosen, land);
        PendingInteraction.ExiledCardMayPlayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), notChosen.getId());

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosen.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, notChosen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chosen card remains castable with any color after Author leaves")
    void chosenCardRemainsCastableAfterAuthorLeaves() {
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(chosen, new Forest()));
        Permanent author = castAuthor();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, author);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent castAuthor() {
        harness.setHand(player1, List.of(new AuthorOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Author of Shadows");
    }
}
