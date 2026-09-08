package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
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

@CardUsed({RuinsOfOranRief.class, Memnite.class, GrizzlyBears.class})
class RuinsOfOranRiefTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for one colorless mana")
    void entersTappedAndTapsForColorlessMana() {
        harness.setHand(player1, List.of(new RuinsOfOranRief()));
        harness.playLand(player1, 0);

        Permanent land = findPermanent(player1, "Ruins of Oran-Rief");
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a counter on a colorless creature that entered this turn")
    void putsCounterOnEligibleCreature() {
        harness.addToBattlefield(player1, new RuinsOfOranRief());
        Card creature = new Memnite();
        harness.setHand(player1, List.of(creature));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, creature);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target a colorless creature an opponent controls")
    void canTargetOpponentsColorlessCreature() {
        harness.addToBattlefield(player1, new RuinsOfOranRief());
        Card creature = new Memnite();
        harness.setHand(player2, List.of(creature));
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, creature);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a colored creature")
    void cannotTargetColoredCreature() {
        harness.addToBattlefield(player1, new RuinsOfOranRief());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("colorless creature");
    }

    @Test
    @DisplayName("Cannot target a colorless creature that entered on an earlier turn")
    void cannotTargetCreatureThatEnteredEarlier() {
        harness.addToBattlefield(player1, new RuinsOfOranRief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Memnite());

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
