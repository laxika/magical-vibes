package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SerratedArrowsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three arrowhead counters")
    void entersWithThreeArrowheadCounters() {
        harness.setHand(player1, List.of(new SerratedArrows()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        UUID arrowsId = harness.getPermanentId(player1, "Serrated Arrows");
        Permanent arrows = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(arrowsId))
                .findFirst()
                .orElseThrow();
        assertThat(arrows.getCounterCount(CounterType.ARROWHEAD)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability removes an arrowhead counter and puts a -1/-1 counter on target creature")
    void abilityMovesCounterToTargetCreature() {
        Permanent arrows = addArrows(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(arrows.getCounterCount(CounterType.ARROWHEAD)).isEqualTo(2);
        assertThat(arrows.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger sacrifices the artifact when no arrowhead counters remain")
    void upkeepSacrificesWithoutCounters() {
        Permanent arrows = addArrows(player1);
        arrows.setCounterCount(CounterType.ARROWHEAD, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the sacrifice trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(arrows);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serrated Arrows"));
    }

    @Test
    @DisplayName("Upkeep trigger leaves the artifact alone while counters remain")
    void upkeepKeepsArtifactWithCounters() {
        Permanent arrows = addArrows(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(arrows);
        assertThat(arrows.getCounterCount(CounterType.ARROWHEAD)).isEqualTo(3);
    }

    private Permanent addArrows(Player owner) {
        Permanent perm = new Permanent(new SerratedArrows());
        perm.setCounterCount(CounterType.ARROWHEAD, 3);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }
}
