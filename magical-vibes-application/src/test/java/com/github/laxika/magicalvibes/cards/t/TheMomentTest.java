package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMoment.class, Disenchant.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class TheMomentTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger puts a time counter on The Moment")
    void upkeepTriggerAddsTimeCounter() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(moment.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps and phases out a target creature until The Moment leaves")
    void untapsAndPhasesOutTargetCreatureUntilSourceLeaves() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, moment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "The Moment");
    }

    @Test
    @DisplayName("Destroys nonland permanents at or below its time-counter threshold, then sacrifices itself")
    void destroysPermanentsAtOrBelowTimeCounterThreshold() {
        Permanent moment = harness.addToBattlefieldAndReturn(player1, new TheMoment());
        Permanent oneManaPermanent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent twoManaPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        moment.setCounterCount(CounterType.TIME, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(twoManaPermanent, land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(oneManaPermanent);
        harness.assertInGraveyard(player1, "The Moment");
    }

    @Test
    @DisplayName("The phasing ability cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new TheMoment());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
