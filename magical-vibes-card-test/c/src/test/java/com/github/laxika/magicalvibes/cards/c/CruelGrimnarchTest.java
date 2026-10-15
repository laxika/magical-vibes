package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NimraiserPaladin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CruelGrimnarch.class, NimraiserPaladin.class})
class CruelGrimnarchTest extends BaseCardTest {

    @Test
    @DisplayName("When Cruel Grimnarch enters, an opponent discards a card")
    void opponentDiscardsACard() {
        NimraiserPaladin discarded = new NimraiserPaladin();
        harness.setHand(player2, List.of(discarded));
        harness.castFromHand(player1, new CruelGrimnarch(), "{5}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Nimraiser Paladin");
    }

    @Test
    @DisplayName("When an opponent has no cards, Cruel Grimnarch gains 4 life")
    void gainsLifeForOpponentUnableToDiscard() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new CruelGrimnarch(), "{5}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Life gain happens during the enters ability, without a separate stack entry")
    void gainsLifeDuringTheOriginalAbilityResolution() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new CruelGrimnarch(), "{5}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent chooses exactly one card and neither player gains life")
    void opponentChoosesWhichCardToDiscard() {
        CruelGrimnarch retained = new CruelGrimnarch();
        CruelGrimnarch discarded = new CruelGrimnarch();
        harness.setHand(player2, List.of(retained, discarded));
        harness.castFromHand(player1, new CruelGrimnarch(), "{5}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deathtouch kills a creature that survives the amount of damage dealt")
    void deathtouchKillsLargerBlocker() {
        Permanent attacker = addCreatureReady(player1, new CruelGrimnarch());
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CruelGrimnarch());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Cruel Grimnarch");
        harness.assertInGraveyard(player2, "Cruel Grimnarch");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
