package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrovaxTheCursed.class, CravenGiant.class})
class CrovaxTheCursedTest extends BaseCardTest {

    @Test
    @DisplayName("Crovax enters with four +1/+1 counters")
    void entersWithCounters() {
        harness.setHand(player1, List.of(new CrovaxTheCursed()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Crovax the Cursed");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the upkeep sacrifice removes a +1/+1 counter")
    void declineRemovesCounter() {
        Permanent source = addReadyCrovax();
        harness.addToBattlefield(player1, new CravenGiant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Craven Giant");
    }

    @Test
    @DisplayName("Sacrificing a creature puts a +1/+1 counter on Crovax")
    void sacrificeAddsCounter() {
        Permanent source = addReadyCrovax();
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CravenGiant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Craven Giant");
    }

    @Test
    @DisplayName("Crovax may sacrifice itself for its upkeep ability")
    void canSacrificeItself() {
        addReadyCrovax();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Crovax the Cursed");
        harness.assertInGraveyard(player1, "Crovax the Cursed");
    }

    @Test
    @DisplayName("Removing Crovax's last counter causes it to die")
    void removingLastCounterCausesDeath() {
        Permanent source = addReadyCrovax();
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Crovax the Cursed");
        harness.assertInGraveyard(player1, "Crovax the Cursed");
    }

    @Test
    @DisplayName("{B} grants flying until end of turn")
    void activatesFlying() {
        Permanent source = addReadyCrovax();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyCrovax() {
        Permanent source = addCreatureReady(player1, new CrovaxTheCursed());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        return source;
    }
}
