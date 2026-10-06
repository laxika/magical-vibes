package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlienSymbiosis;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SilverSableMercenaryLeader.class, GrizzlyBears.class, Forest.class, SpiderSuit.class, AlienSymbiosis.class})
class SilverSableMercenaryLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter on another target creature")
    void entersWithCounterOnAnotherCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SilverSableMercenaryLeader()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with its enters ability")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new SilverSableMercenaryLeader()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking lets it grant lifelink to a modified creature you control")
    void attackingGrantsLifelinkToModifiedCreatureYouControl() {
        Permanent sable = addCreatureReady(player1, new SilverSableMercenaryLeader());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(sable.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an unmodified or opponent-controlled creature")
    void cannotTargetIllegalCreature() {
        addCreatureReady(player1, new SilverSableMercenaryLeader());
        Permanent modifiedBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        modifiedBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, unmodifiedBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SilverSableMercenaryLeader());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    void entersCanPutCounterOnOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SilverSableMercenaryLeader()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entersWithoutAnotherCreatureDoesNotPutCounterOnItself() {
        harness.setHand(player1, List.of(new SilverSableMercenaryLeader()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent sable = findPermanent(player1, "Silver Sable, Mercenary Leader");
        assertThat(sable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackingCanTargetItselfWithANonPowerCounter() {
        Permanent sable = addCreatureReady(player1, new SilverSableMercenaryLeader());
        sable.setCounterCount(CounterType.STUN, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, sable.getId());
        harness.passBothPriorities();

        assertThat(sable.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void attackingWithoutModifiedCreaturesDoesNotRequireAChoice() {
        Permanent sable = addCreatureReady(player1, new SilverSableMercenaryLeader());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(sable.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    void grantedLifelinkGainsLifeFromCombatDamage() {
        Permanent sable = addCreatureReady(player1, new SilverSableMercenaryLeader());
        sable.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, sable.getId());
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void losingModificationBeforeResolutionMakesTargetIllegal() {
        Permanent sable = addCreatureReady(player1, new SilverSableMercenaryLeader());
        sable.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, sable.getId());
        sable.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(sable.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    void losingModificationAfterResolutionDoesNotRemoveLifelink() {
        Permanent sable = addCreatureReady(player1, new SilverSableMercenaryLeader());
        sable.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, sable.getId());
        harness.passBothPriorities();
        sable.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(sable.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void opponentsEquipmentStillMakesYourCreatureModified() {
        Permanent sable = addCreatureReady(player1, new SilverSableMercenaryLeader());
        Permanent suit = harness.addToBattlefieldAndReturn(player2, new SpiderSuit());
        suit.setAttachedTo(sable.getId());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, sable.getId());
        harness.passBothPriorities();

        assertThat(sable.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void ownAuraMakesCreatureModified() {
        Permanent sable = addCreatureReady(player1, new SilverSableMercenaryLeader());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlienSymbiosis());
        aura.setAttachedTo(sable.getId());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, sable.getId());
        harness.passBothPriorities();

        assertThat(sable.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void opponentsAuraAloneDoesNotMakeCreatureModified() {
        Permanent sable = addCreatureReady(player1, new SilverSableMercenaryLeader());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new AlienSymbiosis());
        aura.setAttachedTo(sable.getId());

        declareAttackers(List.of(0));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, sable.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(sable.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
    }
}
