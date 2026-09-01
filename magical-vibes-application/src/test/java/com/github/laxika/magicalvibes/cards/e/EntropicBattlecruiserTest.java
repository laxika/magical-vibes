package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntropicBattlecruiser.class, Distress.class, GrizzlyBears.class})
class EntropicBattlecruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Station uses the tapped creature's power")
    void stationUsesTappedCreaturePower() {
        Permanent battlecruiser = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(battlecruiser), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(battlecruiser.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Eight charge counters animate the Spacecraft and grant flying and deathtouch")
    void eightChargeCountersUnlockAbilities() {
        Permanent battlecruiser = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());

        battlecruiser.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, battlecruiser)).isFalse();
        assertThat(gqs.hasKeyword(gd, battlecruiser, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, battlecruiser, Keyword.DEATHTOUCH)).isFalse();

        battlecruiser.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, battlecruiser)).isTrue();
        assertThat(gqs.hasKeyword(gd, battlecruiser, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, battlecruiser, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("An opponent loses 3 life after discarding while the Battlecruiser has charge")
    void opponentLosesLifeOnDiscardWithChargeCounter() {
        Permanent battlecruiser = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        battlecruiser.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player2, 20);

        discardWithDistress();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An opponent discard does not cause life loss before the first charge counter")
    void opponentDoesNotLoseLifeWithoutChargeCounter() {
        harness.addToBattlefield(player1, new EntropicBattlecruiser());
        harness.setLife(player2, 20);

        discardWithDistress();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Attacking makes an opponent with no cards lose 3 life")
    void attackingOpponentWithEmptyHandCausesLifeLoss() {
        Permanent battlecruiser = addCreatureReady(player1, new EntropicBattlecruiser());
        battlecruiser.setCounterCount(CounterType.CHARGE, 8);
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Attacking makes an opponent discard, then the discard trigger causes 3 life loss")
    void attackingOpponentWithCardsCausesDiscardAndLifeLoss() {
        Permanent battlecruiser = addCreatureReady(player1, new EntropicBattlecruiser());
        battlecruiser.setCounterCount(CounterType.CHARGE, 8);
        Card discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    private void discardWithDistress() {
        Card discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }
}
