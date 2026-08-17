package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TidalInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one tide counter and weakens all blue creatures")
    void entersWithOneTideCounterAndWeakensBlueCreatures() {
        Permanent influence = castTidalInfluence(player1);
        Permanent ownWizard = addToBattlefield(player1, new FugitiveWizard());
        Permanent opponentWizard = addToBattlefield(player2, new FugitiveWizard());
        Permanent bears = addToBattlefield(player1, new GrizzlyBears());

        assertThat(influence.getCounterCount(CounterType.TIDE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownWizard)).isEqualTo(-1);
        assertThat(gqs.getEffectivePower(gd, opponentWizard)).isEqualTo(-1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two tide counters leave blue creatures unchanged")
    void twoTideCountersLeaveBlueCreaturesUnchanged() {
        Permanent influence = addToBattlefield(player1, new TidalInfluence());
        influence.setCounterCount(CounterType.TIDE, 2);
        Permanent wizard = addToBattlefield(player1, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exactly three tide counters strengthen all blue creatures")
    void threeTideCountersStrengthenBlueCreatures() {
        Permanent influence = addToBattlefield(player1, new TidalInfluence());
        influence.setCounterCount(CounterType.TIDE, 3);
        Permanent wizard = addToBattlefield(player1, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four tide counters are removed by the state-triggered ability")
    void fourTideCountersAreRemoved() {
        Permanent influence = addToBattlefield(player1, new TidalInfluence());
        influence.setCounterCount(CounterType.TIDE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(influence.getCounterCount(CounterType.TIDE)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(influence.getCounterCount(CounterType.TIDE)).isZero();
    }

    private Permanent castTidalInfluence(Player player) {
        harness.setHand(player, List.of(new TidalInfluence()));
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.castEnchantment(player, 0);
        harness.passBothPriorities();
        return findPermanent(player, "Tidal Influence");
    }

    private Permanent addToBattlefield(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
