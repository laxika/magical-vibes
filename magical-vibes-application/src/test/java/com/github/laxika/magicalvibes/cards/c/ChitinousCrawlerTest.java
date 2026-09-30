package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChitinousCrawler.class, GrizzlyBears.class, Plains.class})
class ChitinousCrawlerTest extends BaseCardTest {

    @Test
    void beginningOfCombatConjuresDuplicateOfTargetCreatureCard() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new ChitinousCrawler());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).hasSize(2);
        assertThat(graveyard).contains(target);
        assertThat(graveyard.stream()
                .filter(card -> !card.getId().equals(target.getId()))
                .map(Card::getName)
                .toList()).containsExactly("Grizzly Bears");
    }

    @Test
    void descendExilesPermanentCardAndAllowsPlayingIt() {
        GrizzlyBears target = new GrizzlyBears();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(target);
        IntStream.range(0, 7).mapToObj(ignored -> (Card) new Plains()).forEach(graveyard::add);
        harness.setGraveyard(player1, graveyard);
        harness.addToBattlefield(player1, new ChitinousCrawler());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ActivatedAbilityGraveyardExileCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactlyElementsOf(graveyard);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(target.getId(), player1.getId());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void descendCannotBeActivatedWithFewerThanEightPermanentCards() {
        harness.setGraveyard(player1, IntStream.range(0, 7)
                .mapToObj(ignored -> (Card) new Plains())
                .toList());
        harness.addToBattlefield(player1, new ChitinousCrawler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("8 or more permanent cards");
    }
}
