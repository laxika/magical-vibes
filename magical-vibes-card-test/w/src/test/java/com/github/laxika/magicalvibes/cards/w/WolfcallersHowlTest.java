package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolfcallersHowl.class, Forest.class})
class WolfcallersHowlTest extends BaseCardTest {

    @Test
    void createsOneWolfForOpponentWithAtLeastFourCardsInHand() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }

    @Test
    void doesNotCreateWolfForOpponentWithFewerThanFourCardsInHand() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    void checksOpponentsHandsWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    void createsWolfWhenOpponentReachesFourCardsBeforeResolution() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }

    @Test
    void doesNotCountControllersHand() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    void createsOnlyOneWolfForOneOpponentWithMoreThanFourCards() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
        assertThat(findPermanents(player2, "Wolf")).isEmpty();
        var wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).contains(CardSubtype.WOLF);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    void triggerStillCreatesWolfAfterEnchantmentLeavesBattlefield() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        var enchantment = findPermanent(player1, "Wolfcaller's Howl");
        gd.playerBattlefields.get(player1.getId()).remove(enchantment);
        gd.playerGraveyards.get(player1.getId()).add(enchantment.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }
}
