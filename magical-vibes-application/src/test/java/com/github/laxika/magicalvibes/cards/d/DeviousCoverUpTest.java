package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviousCoverUpTest extends BaseCardTest {

    @Test
    @DisplayName("Counters and exiles a spell, then shuffles up to four chosen cards")
    void countersExilesAndShufflesFourCards() {
        Card target = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        List<Card> buried = List.of(
                new LightningBolt(), new GrizzlyBears(), new LightningBolt(),
                new GrizzlyBears(), new LightningBolt());
        harness.setGraveyard(player2, buried);
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, target.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds()).containsExactlyElementsOf(
                buried.stream().map(Card::getId).toList());

        harness.handleMultipleCardsChosen(player2, buried.subList(0, 4).stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .contains(buried.get(4))
                .doesNotContainAnyElementsOf(buried.subList(0, 4));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 4);
    }

    @Test
    @DisplayName("Declining the graveyard targets still exiles the countered spell")
    void decliningGraveyardTargetsStillExilesSpell() {
        Card target = new GrizzlyBears();
        Card buried = new LightningBolt();
        harness.setGraveyard(player2, List.of(buried));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.handleMultipleCardsChosen(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
    }

    @Test
    @DisplayName("With no graveyard cards, the counter target is still exiled")
    void countersAndExilesWithEmptyGraveyard() {
        Card target = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertInGraveyard(player2, "Devious Cover-Up");
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a spell on the stack")
    void cannotTargetPermanent() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, target);
        UUID permanentId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new DeviousCoverUp()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }
}
