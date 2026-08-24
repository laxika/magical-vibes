package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BoundingWolf;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UntamedPup;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoundTamer.class, UntamedPup.class, BoundingWolf.class, GrizzlyBears.class})
class HoundTamerTest extends BaseCardTest {

    @Test
    void activatedAbilityPutsCounterOnAnyTargetCreature() {
        harness.addToBattlefield(player1, new HoundTamer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BoundingWolf());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void untamedPupGivesOtherWolvesAndWerewolvesTrample() {
        gd.dayNight = DayNight.NIGHT;
        Permanent pup = harness.addToBattlefieldAndReturn(player1, new HoundTamer());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new BoundingWolf());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(pup.getCard()).isInstanceOf(UntamedPup.class);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void dayNightTransformsBothFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent tamer = harness.addToBattlefieldAndReturn(player1, new HoundTamer());

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(tamer.getCard()).isInstanceOf(UntamedPup.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(tamer.getCard()).isInstanceOf(HoundTamer.class);
    }

    @Test
    void untamedPupRetainsTheCounterAbility() {
        gd.dayNight = DayNight.NIGHT;
        Permanent pup = harness.addToBattlefieldAndReturn(player1, new HoundTamer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BoundingWolf());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(pup.getCard()).isInstanceOf(UntamedPup.class);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToUntap(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
