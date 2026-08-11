package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CentaurBattlemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Centaur Battlemaster puts three +1/+1 counters on it")
    void castingSpellThatTargetsBattlemasterTriggersHeroic() {
        harness.addToBattlefield(player1, new CentaurBattlemaster());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID battlemasterId = harness.getPermanentId(player1, "Centaur Battlemaster");
        harness.castInstant(player1, 0, battlemasterId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battlemaster = findPermanent(player1, "Centaur Battlemaster");
        assertThat(battlemaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Centaur Battlemaster")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new CentaurBattlemaster());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent battlemaster = findPermanent(player1, "Centaur Battlemaster");
        assertThat(battlemaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Centaur Battlemaster does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new CentaurBattlemaster());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        UUID battlemasterId = harness.getPermanentId(player1, "Centaur Battlemaster");
        harness.castInstant(player2, 0, battlemasterId);
        harness.passBothPriorities();

        Permanent battlemaster = findPermanent(player1, "Centaur Battlemaster");
        assertThat(battlemaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
