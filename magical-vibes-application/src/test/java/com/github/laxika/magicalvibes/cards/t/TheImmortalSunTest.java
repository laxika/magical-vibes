package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheImmortalSunTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+1")
    void boostsOwnCreatures() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new TheImmortalSun());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Spells you cast cost {1} less to cast")
    void reducesControllerSpellCosts() {
        harness.addToBattlefield(player1, new TheImmortalSun());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The cost reduction does not affect opponents' spells")
    void doesNotReduceOpponentSpellCosts() {
        harness.addToBattlefield(player1, new TheImmortalSun());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller draws an additional card during their draw step")
    void drawsAdditionalCardOnControllerDrawStep() {
        harness.addToBattlefield(player1, new TheImmortalSun());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Players cannot activate planeswalker loyalty abilities")
    void locksPlaneswalkerLoyaltyAbilities() {
        harness.addToBattlefield(player1, new TheImmortalSun());
        addReadyJace(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("The loyalty lock does not block other activated abilities of planeswalkers")
    void doesNotLockOtherPlaneswalkerAbilities() {
        harness.addToBattlefield(player1, new TheImmortalSun());

        Card planeswalker = new Card();
        planeswalker.setName("Test Planeswalker");
        planeswalker.setType(CardType.PLANESWALKER);
        planeswalker.setManaCost("{3}");
        planeswalker.setColor(CardColor.BLUE);
        planeswalker.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new BoostSelfEffect(1, 1)), "{T}: Test ability"
        ));
        Permanent permanent = new Permanent(planeswalker);
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReadyJace(Player player) {
        Permanent jace = new Permanent(new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        jace.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jace);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jace;
    }
}
