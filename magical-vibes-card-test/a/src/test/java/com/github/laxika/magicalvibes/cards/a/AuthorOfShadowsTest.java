package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({AuthorOfShadows.class, Forest.class, GrizzlyBears.class, Panharmonicon.class})
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

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, author));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Own graveyard is untouched and an empty opposing graveyard requires no choice")
    void leavesOwnGraveyardUntouched() {
        GrizzlyBears ownCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of());

        castAuthor();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("A graveyard containing only lands is exiled without granting permission")
    void exilesLandOnlyGraveyard() {
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(land));

        Permanent author = castAuthor();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(author.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing a nonland card is mandatory, and lands cannot be chosen")
    void cannotDeclineChoiceOrChooseLand() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(first, second, land));
        castAuthor();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The chosen spell still requires payment of its mana cost")
    void chosenSpellIsNotFree() {
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(chosen));
        castAuthor();

        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosen.getId());
    }

    @Test
    @DisplayName("Permission to cast the chosen creature does not bypass normal timing")
    void chosenCreatureRequiresNormalTiming() {
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(chosen));
        castAuthor();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing Author before its trigger resolves does not prevent exile or casting")
    void resolvesAfterSourceLeaves() {
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(chosen));
        harness.castFromHand(player1, new AuthorOfShadows(), "{4}{B}");
        harness.passBothPriorities();
        Permanent author = findPermanent(player1, "Author of Shadows");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, author));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A doubled trigger cannot choose cards exiled by the previous resolution")
    void doubledTriggerCannotChoosePreviouslyExiledCards() {
        GrizzlyBears chosen = new GrizzlyBears();
        GrizzlyBears notChosen = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(chosen, notChosen));
        harness.addToBattlefield(player1, new Panharmonicon());
        castAuthor();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId())
                .doesNotContainKey(notChosen.getId());
    }

    private Permanent castAuthor() {
        harness.castFromHand(player1, new AuthorOfShadows(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Author of Shadows");
    }
}
