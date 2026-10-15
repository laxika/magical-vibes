package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EchoingReturn.class, GrizzlyBears.class, HolyDay.class})
class EchoingReturnTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureAndAllSameNameCardsToHand() {
        Card target = new GrizzlyBears();
        Card sameName = new GrizzlyBears();
        Card other = new HolyDay();
        EchoingReturn spell = new EchoingReturn();
        harness.setGraveyard(player1, List.of(target, sameName, other));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(target.getId(), sameName.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(other.getId(), spell.getId());
    }

    @Test
    void cannotTargetNonCreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new EchoingReturn()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureInOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new EchoingReturn()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsOnlyTargetWhenThereAreNoOtherCardsWithItsName() {
        Card target = new GrizzlyBears();
        EchoingReturn spell = new EchoingReturn();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void leavesSameNameCardsInOpponentsGraveyard() {
        Card target = new GrizzlyBears();
        Card opponentCopy = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setGraveyard(player2, List.of(opponentCopy));
        harness.setHand(player1, List.of(new EchoingReturn()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCopy);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(opponentCopy);
    }

    @Test
    void returnsNothingWhenTargetLeavesGraveyardBeforeResolution() {
        Card target = new GrizzlyBears();
        Card sameName = new GrizzlyBears();
        EchoingReturn spell = new EchoingReturn();
        harness.setGraveyard(player1, List.of(target, sameName));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(sameName));
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(sameName, spell);
    }

    @Test
    void returnsSameNameCardThatEntersGraveyardAfterCasting() {
        Card target = new GrizzlyBears();
        Card laterCopy = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new EchoingReturn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(target, laterCopy));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(target, laterCopy);
    }
}
