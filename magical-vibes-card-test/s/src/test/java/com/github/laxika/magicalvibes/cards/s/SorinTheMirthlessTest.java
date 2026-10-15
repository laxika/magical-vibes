package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SorinTheMirthless.class, DoomedDissenter.class, Swamp.class})
class SorinTheMirthlessTest extends BaseCardTest {

    @Test
    @DisplayName("+1 lets you reveal the top card into your hand and lose its mana value")
    void plusOneAcceptsTopCardAndLosesManaValue() {
        Permanent sorin = addReadySorin(player1, 4);
        Card topCard = new DoomedDissenter();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("+1 can be declined, leaving the top card in place without life loss")
    void plusOneDeclinesTopCard() {
        Permanent sorin = addReadySorin(player1, 4);
        Card topCard = new DoomedDissenter();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("-2 creates a 2/3 black Vampire with flying and lifelink")
    void minusTwoCreatesVampireToken() {
        Permanent sorin = addReadySorin(player1, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Vampire");
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("-7 deals 13 damage to a player and gains 13 life")
    void minusSevenDealsDamageAndGainsLife() {
        Permanent sorin = addReadySorin(player1, 7);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(33);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("+1 on an empty library does nothing and does not count as drawing")
    void plusOneOnEmptyLibrary() {
        Permanent sorin = addReadySorin(player1, 4);
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("+1 can put a land into hand without losing life")
    void plusOneAcceptsLand() {
        addReadySorin(player1, 4);
        Card land = new Swamp();
        harness.setLibrary(player1, List.of(land));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("-7 can deal lethal damage to a creature and still gain 13 life")
    void minusSevenTargetsCreature() {
        addReadySorin(player1, 7);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DoomedDissenter());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Doomed Dissenter");
        harness.assertNotOnBattlefield(player2, "Doomed Dissenter");
        harness.assertLife(player1, 33);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Sorin the Mirthless");
    }

    @Test
    @DisplayName("-7 can target a planeswalker and removes 13 loyalty")
    void minusSevenTargetsPlaneswalker() {
        addReadySorin(player1, 7);
        Permanent target = addReadySorin(player2, 15);
        harness.forceActivePlayer(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Sorin the Mirthless");
        harness.assertLife(player1, 33);
    }

    @Test
    @DisplayName("-7 gains no life if its only target leaves before resolution")
    void minusSevenIllegalTargetPreventsLifeGain() {
        addReadySorin(player1, 8);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DoomedDissenter());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setExile(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("-7 targeting yourself gains life before checking for a life-total loss")
    void minusSevenSelfTargetSurvivesTemporaryNegativeLife() {
        addReadySorin(player1, 7);
        harness.setLife(player1, 5);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 5);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private Permanent addReadySorin(Player player, int loyalty) {
        Permanent sorin = harness.addToBattlefieldAndReturn(player, new SorinTheMirthless());
        sorin.setCounterCount(CounterType.LOYALTY, loyalty);
        sorin.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return sorin;
    }
}
