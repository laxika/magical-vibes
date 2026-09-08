package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LazavFamiliarStranger.class, TomeScour.class, GrizzlyBears.class, Opt.class})
class LazavFamiliarStrangerTest extends BaseCardTest {

    @Test
    @DisplayName("Commits a crime, adds a counter, and may copy an exiled creature card until end of turn")
    void addsCounterAndCopiesCreatureCard() {
        Permanent lazav = addLazav();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        commitCrime();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(lazav.getCard()).isNotSameAs(lazav.getOriginalCard());
        assertThat(lazav.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, lazav)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lazav)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Does not offer the copy choice for a noncreature card")
    void noncreatureCardDoesNotOfferCopyChoice() {
        Permanent lazav = addLazav();
        Card opt = new Opt();
        harness.setGraveyard(player2, List.of(opt));

        commitCrime();
        harness.handleMultipleCardsChosen(player1, List.of(opt.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(lazav.getCard()).isSameAs(lazav.getOriginalCard());
        assertThat(lazav.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, lazav)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lazav)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opt);
    }

    @Test
    @DisplayName("Declining the copy choice leaves Lazav unchanged after exiling a creature card")
    void decliningCopyChoiceLeavesLazavUnchanged() {
        Permanent lazav = addLazav();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        commitCrime();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(lazav.getCard()).isSameAs(lazav.getOriginalCard());
        assertThat(lazav.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, lazav)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lazav)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
    }

    private Permanent addLazav() {
        return harness.addToBattlefieldAndReturn(player1, new LazavFamiliarStranger());
    }

    private void commitCrime() {
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
