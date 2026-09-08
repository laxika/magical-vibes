package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TeferiTimebender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IchormoonGauntletTest extends BaseCardTest {

    @Test
    void grantsProliferateToPlaneswalkersYouControl() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent teferi = addReadyTeferi(player1, 5);

        int teferiIndex = gd.playerBattlefields.get(player1.getId()).indexOf(teferi);
        harness.activateAbility(player1, teferiIndex, 3, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(teferi.getId()));

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void grantsExtraTurnAbilityToPlaneswalkersYouControl() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent teferi = addReadyTeferi(player1, 12);

        int teferiIndex = gd.playerBattlefields.get(player1.getId()).indexOf(teferi);
        harness.activateAbility(player1, teferiIndex, 4, null, null);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).contains(player1.getId());
    }

    @Test
    void noncreatureSpellTriggersChosenCounterAddition() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void creatureSpellDoesNotTriggerCounterAddition() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Ichormoon Gauntlet"));
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent teferi = new Permanent(new TeferiTimebender());
        teferi.setCounterCount(CounterType.LOYALTY, loyalty);
        teferi.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(teferi);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return teferi;
    }
}
