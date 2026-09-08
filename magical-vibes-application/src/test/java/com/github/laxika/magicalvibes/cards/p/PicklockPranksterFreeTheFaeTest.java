package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FreeTheFae;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PicklockPranksterFreeTheFae.class, FreeTheFae.class, GrizzlyBears.class, Opt.class, Zombify.class})
class PicklockPranksterFreeTheFaeTest extends BaseCardTest {

    @Test
    void freeTheFaeMillsFourAndReturnsOneInstantSorceryOrFaerie() {
        Card nonmatching = new GrizzlyBears();
        Card instant = new Opt();
        Card sorcery = new Zombify();
        Card faerie = new PicklockPranksterFreeTheFae();
        harness.setLibrary(player1, List.of(nonmatching, instant, sorcery, faerie));

        PicklockPranksterFreeTheFae card = new PicklockPranksterFreeTheFae();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactlyInAnyOrder(
                indexOf(graveyard, instant), indexOf(graveyard, sorcery), indexOf(graveyard, faerie));
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleGraveyardCardChosen(player1, indexOf(graveyard, instant));

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(nonmatching, sorcery, faerie)
                .doesNotContain(instant);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void freeTheFaeDoesNothingAfterMillingWhenNoEligibleCardWasMilled() {
        List<Card> milled = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, milled);

        PicklockPranksterFreeTheFae card = new PicklockPranksterFreeTheFae();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(milled);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        harness.setLibrary(player1, List.of());
        PicklockPranksterFreeTheFae card = new PicklockPranksterFreeTheFae();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    private int indexOf(List<Card> cards, Card card) {
        return IntStream.range(0, cards.size())
                .filter(index -> cards.get(index).getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
