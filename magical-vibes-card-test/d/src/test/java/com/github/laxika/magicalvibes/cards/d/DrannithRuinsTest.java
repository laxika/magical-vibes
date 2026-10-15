package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({DrannithRuins.class, GrizzlyBears.class, EliteVanguard.class})
class DrannithRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one colorless mana")
    void tapsForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DrannithRuins());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts two counters on a non-Human creature that entered this turn")
    void putsTwoCountersOnEligibleCreature() {
        harness.addToBattlefield(player1, new DrannithRuins());
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a Human creature")
    void cannotTargetHumanCreature() {
        harness.addToBattlefield(player1, new DrannithRuins());
        Permanent human = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Human creature");
    }

    @Test
    @DisplayName("Cannot target a creature that entered on an earlier turn")
    void cannotTargetCreatureThatEnteredEarlier() {
        harness.addToBattlefield(player1, new DrannithRuins());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn");
    }

    @Test
    @DisplayName("Can put counters on an opponent's creature that entered this turn")
    void canTargetOpponentsNewCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DrannithRuins());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a Human even when it entered this turn")
    void cannotTargetNewHumanCreature() {
        harness.addToBattlefield(player1, new DrannithRuins());
        Permanent human = harness.enterBattlefieldAndReturn(player2, new EliteVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Human creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even if it entered this turn")
    void cannotTargetNewNoncreature() {
        harness.addToBattlefield(player1, new DrannithRuins());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new DrannithRuins());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the counter ability with only one mana")
    void requiresTwoMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DrannithRuins());
        Permanent target = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not place counters if the target leaves before resolution")
    void targetLeavingBattlefieldPreventsCounters() {
        harness.addToBattlefield(player1, new DrannithRuins());
        Permanent target = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getOriginalCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
