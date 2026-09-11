package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpineseekerCentipede.class, Forest.class, GrizzlyBears.class, LeoninScimitar.class,
        Pacifism.class, Plains.class, Shock.class})
class SpineseekerCentipedeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a 2/1 without delirium")
    void noDelirium() {
        Permanent centipede = addCentipede(List.of(new Forest(), new Shock(), new LeoninScimitar()));

        assertThat(gqs.getEffectivePower(gd, centipede)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, centipede)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, centipede, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+2 and vigilance with delirium")
    void delirium() {
        Permanent centipede = addCentipede(List.of(
                new Forest(), new Shock(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.getEffectivePower(gd, centipede)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centipede)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, centipede, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("ETB searches for a basic land and puts it into its controller's hand")
    void searchesForBasicLand() {
        harness.setHand(player1, List.of(new SpineseekerCentipede()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Plains");
    }

    private Permanent addCentipede(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        return harness.addToBattlefieldAndReturn(player1, new SpineseekerCentipede());
    }
}
