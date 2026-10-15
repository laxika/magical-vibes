package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FirstLittlePig;
import com.github.laxika.magicalvibes.cards.s.SecondLittlePig;
import com.github.laxika.magicalvibes.cards.s.SwineRebellion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThirdLittlePig.class, SwineRebellion.class, FirstLittlePig.class, SecondLittlePig.class})
class ThirdLittlePigTest extends BaseCardTest {

    @Test
    void conjuringTwoOtherCardsTogetherGivesOnePerpetualBoost() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new ThirdLittlePig());

        conjureOtherPigs(player1);

        assertThat(gqs.getEffectivePower(gd, pig)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pig)).isEqualTo(3);
        assertThat(pig.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void graveyardPigGetsBoostThatSurvivesBeingCast() {
        ThirdLittlePig card = new ThirdLittlePig();
        harness.setGraveyard(player1, List.of(card));

        conjureOtherPigs(player1);
        gd.playerGraveyards.get(player1.getId()).remove(card);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent pig = findPermanent(player1, "Third Little Pig");
        assertThat(gqs.getEffectivePower(gd, pig)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pig)).isEqualTo(3);
    }

    @Test
    void opponentsConjureDoesNotBoostPig() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new ThirdLittlePig());

        conjureOtherPigs(player2);

        assertThat(gqs.getEffectivePower(gd, pig)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pig)).isEqualTo(2);
    }

    @Test
    void battlefieldBoostSurvivesLeavingAndReturning() {
        ThirdLittlePig card = new ThirdLittlePig();
        Permanent original = harness.addToBattlefieldAndReturn(player1, card);
        conjureOtherPigs(player1);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Third Little Pig");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    private void conjureOtherPigs(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player, List.of(new SwineRebellion()));
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player, 0, 0);
        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        List<Card> cards = choice.cards().stream()
                .filter(card -> !card.getName().equals("Third Little Pig"))
                .toList();
        harness.handleMultipleCardsChosen(player, cards.stream().map(Card::getId).toList());
        PendingInteraction.RevealedHandChoice battlefieldChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player, battlefieldChoice.validIndices().getFirst());
        resolveAllTriggers();
    }
}
