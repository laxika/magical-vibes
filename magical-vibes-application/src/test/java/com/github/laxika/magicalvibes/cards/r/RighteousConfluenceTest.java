package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RighteousConfluence.class, GhostlyPrison.class, GrizzlyBears.class})
class RighteousConfluenceTest extends BaseCardTest {

    @Test
    void resolvesAllThreeModes() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GhostlyPrison());
        harness.setLife(player1, 20);

        cast(new int[]{0, 1, 2}, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(enchantment.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Knight"))
                .singleElement()
                .satisfies(knight -> {
                    assertThat(knight.getEffectivePower()).isEqualTo(2);
                    assertThat(knight.getEffectiveToughness()).isEqualTo(2);
                    assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
                });
    }

    @Test
    void repeatedTokenModeCreatesThreeVigilantKnights() {
        cast(new int[]{0, 0, 0}, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Knight"))
                .hasSize(3)
                .allSatisfy(knight -> assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue());
    }

    @Test
    void repeatedLifeModeGainsFifteenLife() {
        harness.setLife(player1, 20);

        cast(new int[]{2, 2, 2}, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(35);
    }

    @Test
    void exileModeRejectsCreatureTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{1, 1, 1}, List.of(creature.getId(), creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modeIndices, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new RighteousConfluence()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices), targetIds);
    }
}
