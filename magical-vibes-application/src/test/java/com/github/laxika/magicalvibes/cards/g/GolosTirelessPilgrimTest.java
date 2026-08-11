package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.ReliquaryTower;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GolosTirelessPilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may search for any land and put it onto the battlefield tapped")
    void etbMaySearchForAnyLand() {
        harness.setHand(player1, List.of(new GolosTirelessPilgrim()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);

        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        Card searchableLand = new ReliquaryTower();
        library.addAll(List.of(searchableLand, new Forest(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards())
                .extracting(Card::getId)
                .contains(searchableLand.getId());
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));

        int choice = 0;
        for (int i = 0; i < search.params().cards().size(); i++) {
            if (search.params().cards().get(i).getId().equals(searchableLand.getId())) {
                choice = i;
                break;
            }
        }
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(choice));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }

    @Test
    @DisplayName("Activated ability exiles the top three cards with free-play permission")
    void activatedAbilityExilesTopThreeForFree() {
        Permanent golos = new Permanent(new GolosTirelessPilgrim());
        golos.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(golos);

        Card first = new Plains();
        Card second = new Forest();
        Card third = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(third, second, first));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(first.getId(), second.getId(), third.getId());
    }

}
