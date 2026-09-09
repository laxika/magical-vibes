package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({QuarantineField.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Naturalize.class})
class QuarantineFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to one nonland permanent per isolation counter")
    void exilesOnePermanentPerIsolationCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castForX(2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Quarantine Field");
        assertThat(source.getCounterCount(CounterType.ISOLATION)).isEqualTo(2);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(enchantment.getOriginalCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId())
                        || permanent.getId().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("Returns all exiled permanents when Quarantine Field leaves")
    void returnsExiledPermanentsWhenSourceLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castForX(2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Quarantine Field");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNull();
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNull();
    }

    @Test
    @DisplayName("Cannot target a land or a permanent you control")
    void rejectsIllegalTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castForX(1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castForX(int xValue) {
        harness.setHand(player1, List.of(new QuarantineField()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue * 2);
        gs.playCard(gd, player1, 0, xValue, null, null, List.of(), List.of());
    }
}
