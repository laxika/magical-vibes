package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CecilyHauntedMage.class, GrizzlyBears.class, Opt.class})
class CecilyHauntedMageTest extends BaseCardTest {

    @Test
    @DisplayName("The controller's maximum hand size is eleven")
    void maximumHandSizeIsEleven() {
        harness.addToBattlefield(player1, new CecilyHauntedMage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, cards(12));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking draws a card and loses one life")
    void attackDrawsAndLosesLife() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, new ArrayList<>());
        addAttackingCecily();
        int startingLife = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("With eleven cards in hand after drawing, the attack trigger offers a free instant")
    void offersFreeInstantWhenHandReachesEleven() {
        Opt opt = new Opt();
        List<Card> hand = new ArrayList<>(cards(9));
        hand.add(opt);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, hand);
        addAttackingCecily();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(opt.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The attack trigger does not offer a spell below eleven cards")
    void doesNotOfferSpellBelowElevenCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, new ArrayList<>(cards(8)));
        addAttackingCecily();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    private Permanent addAttackingCecily() {
        Permanent cecily = addCreatureReady(player1, new CecilyHauntedMage());
        cecily.setAttacking(true);
        return cecily;
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> (Card) new GrizzlyBears())
                .toList();
    }
}
