package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AfterlifeFromTheLoam.class, GrizzlyBears.class, HillGiant.class, HolyDay.class})
class AfterlifeFromTheLoamTest extends BaseCardTest {

    @Test
    void returnsUpToOneCreatureFromEachGraveyardAsZombiesUnderYourControl() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new AfterlifeFromTheLoam()));
        addMana();

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(ownCreature.getId(), opponentCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(ownCreature.getId(), opponentCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentCreature);

        Permanent returnedOwnCreature = findPermanent(player1, "Grizzly Bears");
        Permanent returnedOpponentCreature = findPermanent(player1, "Hill Giant");
        assertThat(returnedOwnCreature.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(returnedOpponentCreature.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(returnedOwnCreature.getCard().getSubtypes()).contains(CardSubtype.BEAR);
    }

    @Test
    void mayChooseNoTargets() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AfterlifeFromTheLoam()));
        addMana();

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Afterlife from the Loam");
    }

    @Test
    void cannotTargetMoreThanOneCardFromTheSameGraveyard() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player1, List.of(new AfterlifeFromTheLoam()));
        addMana();

        harness.castSorcery(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one");
    }

    @Test
    void cannotTargetNonCreatureCards() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new AfterlifeFromTheLoam()));
        addMana();

        harness.castSorcery(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }
}
