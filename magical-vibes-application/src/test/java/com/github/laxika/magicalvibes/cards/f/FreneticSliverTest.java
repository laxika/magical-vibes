package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FreneticSliver.class, MetallicSliver.class, GrizzlyBears.class})
class FreneticSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Frenetic Sliver grants its coin-flip ability to all Slivers")
    void grantsAbilityToAllSlivers() {
        harness.addToBattlefield(player1, new FreneticSliver());
        harness.addToBattlefield(player1, new MetallicSliver());
        harness.addToBattlefield(player2, new MetallicSliver());

        Permanent opposingSliver = findPermanent(player2, "Metallic Sliver");
        int opposingSliverIndex = gd.playerBattlefields.get(player2.getId()).indexOf(opposingSliver);
        harness.activateAbility(player2, opposingSliverIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Metallic Sliver"));
    }

    @Test
    @DisplayName("Frenetic Sliver itself can activate the granted ability")
    void grantsAbilityToItself() {
        Permanent freneticSliver = addCreatureReady(player1, new FreneticSliver());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Frenetic Sliver"));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(freneticSliver);
    }

    @Test
    @DisplayName("A winning flip returns the Sliver at the next end step, while a losing flip sacrifices it")
    void winningFlipReturnsAndLosingFlipSacrifices() {
        Permanent sliver = addCreatureReady(player1, new FreneticSliver());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        boolean wonFlip = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Frenetic Sliver"));
        if (wonFlip) {
            assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(sliver.getCard());
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sliver);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(permanent -> permanent.getCard() instanceof FreneticSliver);
        } else {
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(sliver.getCard());
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sliver);
        }
    }

    @Test
    @DisplayName("Non-Slivers do not gain Frenetic Sliver's ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new FreneticSliver());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
