package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.d.DeathknellKami;
import com.github.laxika.magicalvibes.cards.m.ManrikiGusari;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BriarknitKami.class, ArabaMothrider.class, DeathknellKami.class, ManrikiGusari.class,
        SpiritualVisit.class})
class BriarknitKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell puts a +1/+1 counter on target creature")
    void spiritSpellPutsCounterOnTargetCreature() {
        addBriarknitKami();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());

        harness.castFromHand(player1, new DeathknellKami(), "{1}{B}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an Arcane spell puts a +1/+1 counter on target creature")
    void arcaneSpellPutsCounterOnTargetCreature() {
        addBriarknitKami();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArabaMothrider());

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Briarknit Kami")
    void unrelatedSpellDoesNotTrigger() {
        addBriarknitKami();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArabaMothrider());

        harness.castFromHand(player1, new ManrikiGusari(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The cast trigger cannot target a noncreature permanent")
    void castTriggerCannotTargetNoncreature() {
        addBriarknitKami();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManrikiGusari());

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent casting a Spirit spell does not trigger Briarknit Kami")
    void opponentSpiritSpellDoesNotTrigger() {
        addBriarknitKami();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArabaMothrider());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new DeathknellKami(), "{1}{B}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addBriarknitKami() {
        harness.addToBattlefield(player1, new BriarknitKami());
    }
}
