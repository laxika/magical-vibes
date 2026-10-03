package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoggartPrankster;
import com.github.laxika.magicalvibes.cards.k.KinscaerSentry;
import com.github.laxika.magicalvibes.cards.w.WildUnraveling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChampionOfTheClachan.class, KinscaerSentry.class, BoggartPrankster.class, WildUnraveling.class})
class ChampionOfTheClachanTest extends BaseCardTest {

    @Test
    @DisplayName("Can behold a Kithkin permanent and returns it to its owner's hand when Champion leaves")
    void beholdsPermanentAndReturnsItToHand() {
        Card beheldCard = new KinscaerSentry();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        harness.addToBattlefield(player1, new KinscaerSentry());
        harness.setHand(player1, List.of(new ChampionOfTheClachan()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, beheldPermanent.getId());
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Champion of the Clachan");
        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        Permanent otherKithkin = findPermanent(player1, "Kinscaer Sentry");
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherKithkin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherKithkin)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, champion));

        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(beheldCard);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(beheldCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Can behold a Kithkin card from hand")
    void beholdsCardFromHand(int spellIndex) {
        Card beheldCard = new KinscaerSentry();
        Card bystander = new BoggartPrankster();
        harness.setHand(player1, spellIndex == 0
                ? List.of(new ChampionOfTheClachan(), beheldCard, bystander)
                : List.of(beheldCard, new ChampionOfTheClachan(), bystander));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreatureWithBeholdHandCard(player1, spellIndex, 1 - spellIndex);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bystander);
        Permanent champion = findPermanent(player1, "Champion of the Clachan");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, champion));

        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(beheldCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    @DisplayName("Rejects a non-Kithkin behold choice")
    void rejectsNonKithkinBeholdChoice() {
        Card nonKithkin = new BoggartPrankster();
        harness.setHand(player1, List.of(new ChampionOfTheClachan(), nonKithkin));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreatureWithBeholdHandCard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(nonKithkin);
        assertThat(gd.findExiledCard(nonKithkin.getId())).isNull();
    }
    @Test
    void cannotCastWithoutBeholdingAKithkin() {
        harness.setHand(player1, List.of(new ChampionOfTheClachan()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Champion of the Clachan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBeholdTheSpellItself() {
        harness.setHand(player1, List.of(new ChampionOfTheClachan()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreatureWithBeholdHandCard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Champion of the Clachan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBeholdAnOpponentsKithkin() {
        Permanent opponentKithkin = harness.addToBattlefieldAndReturn(player2, new KinscaerSentry());
        harness.setHand(player1, List.of(new ChampionOfTheClachan()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreatureWithBeholdPermanent(player1, 0, opponentKithkin.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Kinscaer Sentry");
        harness.assertInHand(player1, "Champion of the Clachan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostsOnlyOtherKithkinItsControllerControls() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KinscaerSentry());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KinscaerSentry());
        Permanent nonKithkin = harness.addToBattlefieldAndReturn(player1, new BoggartPrankster());
        harness.addToBattlefield(player1, new ChampionOfTheClachan());

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonKithkin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonKithkin)).isEqualTo(3);
    }

    @Test
    void canBeCastDuringOpponentsCombatAndExilesBeforeResolving() {
        Card beheldCard = new KinscaerSentry();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new ChampionOfTheClachan(), beheldCard));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);

        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        harness.assertNotInHand(player1, "Kinscaer Sentry");
        harness.assertNotOnBattlefield(player1, "Champion of the Clachan");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Champion of the Clachan");
    }

    @Test
    void counteredChampionLeavesTheBeheldCardInExile() {
        Card champion = new ChampionOfTheClachan();
        Card beheldCard = new KinscaerSentry();
        harness.setHand(player1, List.of(champion, beheldCard));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player2, List.of(new WildUnraveling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, champion.getId(), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Champion of the Clachan");
        harness.assertNotOnBattlefield(player1, "Champion of the Clachan");
        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(beheldCard);
    }
}
