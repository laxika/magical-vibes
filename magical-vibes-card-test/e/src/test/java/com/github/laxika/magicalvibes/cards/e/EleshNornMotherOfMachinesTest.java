package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.a.AnnexSentry;
import com.github.laxika.magicalvibes.cards.k.KambalProfiteeringMayor;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.SkrelvsHive;
import com.github.laxika.magicalvibes.cards.z.ZoZuThePunisher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EleshNornMotherOfMachines.class, ElvishVisionary.class, Forest.class,
        IchorWellspring.class, ZoZuThePunisher.class, PropheticPrism.class,
        EvolvingAdaptive.class, AnnexSentry.class, KambalProfiteeringMayor.class, SkrelvsHive.class})
class EleshNornMotherOfMachinesTest extends BaseCardTest {

    @Test
    void doublesOwnCreatureEnterAbility() {
        harness.addToBattlefield(player1, new EleshNornMotherOfMachines());
        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void doublesAbilityOfControlledPermanentWhenOpponentPermanentEnters() {
        harness.addToBattlefield(player1, new EleshNornMotherOfMachines());
        harness.addToBattlefield(player1, new ZoZuThePunisher());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void doesNotAllowOpponentPermanentOwnEnterAbilityToTrigger() {
        harness.addToBattlefield(player1, new EleshNornMotherOfMachines());
        harness.setHand(player2, List.of(new IchorWellspring()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotAllowOpponentPermanentTriggerWhenLandEnters() {
        harness.addToBattlefield(player1, new EleshNornMotherOfMachines());
        harness.addToBattlefield(player2, new ZoZuThePunisher());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void doublesArtifactEntryAndResolvesBothDraws() {
        harness.addToBattlefield(player1, new EleshNornMotherOfMachines());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void enteringWithCountersIsNeitherDoubledNorSuppressed() {
        harness.addToBattlefield(player1, new EleshNornMotherOfMachines());

        var ownAdaptive = harness.enterBattlefieldAndReturn(player1, new EvolvingAdaptive());
        var opposingAdaptive = harness.enterBattlefieldAndReturn(player2, new EvolvingAdaptive());

        assertThat(ownAdaptive.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(opposingAdaptive.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opposingEleshNornsSuppressEntryTriggersForBothPlayers() {
        var ownAdaptive = harness.enterBattlefieldAndReturn(player1, new EvolvingAdaptive());
        harness.addToBattlefield(player2, new EleshNornMotherOfMachines());
        var opposingAdaptive = harness.enterBattlefieldAndReturn(player2, new EvolvingAdaptive());

        harness.enterBattlefieldAndReturn(player1, new EleshNornMotherOfMachines());

        assertThat(gd.stack).isEmpty();
        assertThat(ownAdaptive.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(opposingAdaptive.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void eleshNornsOwnEntryDoublesEvolve() {
        var adaptive = harness.enterBattlefieldAndReturn(player1, new EvolvingAdaptive());

        harness.enterBattlefieldAndReturn(player1, new EleshNornMotherOfMachines());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(adaptive.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    void additionalEntryTriggerCanChooseADifferentTarget() {
        harness.addToBattlefield(player1, new EleshNornMotherOfMachines());
        var first = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        var second = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new AnnexSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    void suppressesOpposingTriggersCausedByTokensEnteringUnderOwnControl() {
        harness.addToBattlefield(player1, new EleshNornMotherOfMachines());
        harness.addToBattlefield(player1, new SkrelvsHive());
        harness.addToBattlefield(player2, new KambalProfiteeringMayor());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
