package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigardianSavior.class, GrizzlyBears.class, HillGiant.class, Zombify.class})
class SigardianSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, returns up to two small creature cards from the graveyard")
    void returnsUpToTwoSmallCreaturesWhenCast() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(first, second, tooExpensive));
        castSigardianSavior();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(tooExpensive.getId());
    }

    @Test
    @DisplayName("Does not return creatures when it enters without being cast")
    void doesNotReturnCreaturesWhenNotCast() {
        Card returnedCard = new GrizzlyBears();
        Card savior = new SigardianSavior();
        Card zombify = new Zombify();
        harness.setGraveyard(player1, List.of(savior, returnedCard));
        harness.setHand(player1, List.of(zombify));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, savior.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(savior.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(returnedCard.getId(), zombify.getId());
    }

    private void castSigardianSavior() {
        harness.setHand(player1, List.of(new SigardianSavior()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
