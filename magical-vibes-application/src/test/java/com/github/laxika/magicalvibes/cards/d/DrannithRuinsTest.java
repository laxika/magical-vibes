package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

        Permanent target = findPermanent(player1, creature);
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

    private Permanent findPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
