package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ScaleDeflection;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadLinnorm.class, ScaleDeflection.class, GrizzlyBears.class, AirElemental.class, Forest.class})
class DreadLinnormTest extends BaseCardTest {

    @Test
    void adventurePutsCountersUntapsAndGrantsHexproof() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        DreadLinnorm card = new DreadLinnorm();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void cannotAdventureTargetANoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        DreadLinnorm card = new DreadLinnorm();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBeBlockedByCreatureWithPowerThreeOrLess() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent linnorm = addCreatureReady(player1, new DreadLinnorm());
        prepareBlockers(linnorm);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(linnorm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByCreatureWithPowerGreaterThanThree() {
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        Permanent linnorm = addCreatureReady(player1, new DreadLinnorm());
        prepareBlockers(linnorm);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(linnorm);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void adventureCanTargetOpponentsCreatureAndHexproofExpires() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new DreadLinnorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureResolves() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        DreadLinnorm card = new DreadLinnorm();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dread Linnorm");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void cannotBeBlockedAtPowerThreeBoundary() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent linnorm = addCreatureReady(player1, new DreadLinnorm());
        prepareBlockers(linnorm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersCanRaiseBlockerAbovePowerRestriction() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent linnorm = addCreatureReady(player1, new DreadLinnorm());
        prepareBlockers(linnorm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void adventureWithMissingTargetGoesToGraveyardWithoutCastPermission() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        DreadLinnorm card = new DreadLinnorm();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAdventure(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dread Linnorm");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    private void prepareBlockers(Permanent linnorm) {
        linnorm.setAttacking(true);
        prepareDeclareBlockers();
    }
}
