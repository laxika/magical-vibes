package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MurderousCut;
import com.github.laxika.magicalvibes.cards.s.SummitProwler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmamentCorps.class, AlpineGrizzly.class, SummitProwler.class, MurderousCut.class})
class ArmamentCorpsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts both +1/+1 counters on one target creature you control")
    void putsBothCountersOnOneTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        castArmamentCorps(List.of(target.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB distributes one +1/+1 counter to each of two target creatures you control")
    void distributesCountersAmongTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SummitProwler());
        castArmamentCorps(List.of(first.getId(), second.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new ArmamentCorps()));
        addArmamentCorpsMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("The entering Corps can put both counters on itself")
    void canTargetItself() {
        harness.castFromHand(player1, new ArmamentCorps(), "{2}{W}{B}{G}");
        harness.passBothPriorities();
        java.util.UUID corpsId = harness.getPermanentId(player1, "Armament Corps");
        harness.handlePermanentChosen(player1, corpsId);
        harness.passBothPriorities();

        Permanent corps = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(corps.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A counter assigned to a removed target is not reassigned to the surviving target")
    void doesNotRedistributeCounterFromIllegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SummitProwler());
        castArmamentCorps(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new MurderousCut()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ETB ability does not resolve when its only target is destroyed")
    void doesNotResolveWithNoLegalTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        castArmamentCorps(List.of(target.getId()));
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new MurderousCut()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        Permanent corps = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(corps.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB ability still resolves after Armament Corps is destroyed")
    void abilitySurvivesRemovalOfSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        castArmamentCorps(List.of(target.getId()));
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new MurderousCut()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Armament Corps"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Armament Corps");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castArmamentCorps(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new ArmamentCorps()));
        addArmamentCorpsMana();
        harness.castCreature(player1, 0, targetIds);
    }

    private void addArmamentCorpsMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
