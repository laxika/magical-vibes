package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.cards.p.Pongify;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({FreneticSliver.class, SinewSliver.class, GossamerPhantasm.class, Pongify.class})
class FreneticSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Frenetic Sliver grants its coin-flip ability to all Slivers")
    void grantsAbilityToAllSlivers() {
        harness.addToBattlefield(player1, new FreneticSliver());
        harness.addToBattlefield(player1, new SinewSliver());
        harness.addToBattlefield(player2, new SinewSliver());

        Permanent opposingSliver = findPermanent(player2, "Sinew Sliver");
        int opposingSliverIndex = gd.playerBattlefields.get(player2.getId()).indexOf(opposingSliver);
        harness.activateAbility(player2, opposingSliverIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Sinew Sliver"));
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
    @DisplayName("If Frenetic Sliver leaves before resolution, its ability does not flip a coin")
    void doesNothingWhenSourceLeavesBeforeResolution() {
        Permanent freneticSliver = addCreatureReady(player1, new FreneticSliver());
        harness.setHand(player1, List.of(new Pongify()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player1, 0, freneticSliver.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(freneticSliver.getCard());
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("coin flip for Frenetic Sliver"));
    }

    @Test
    @DisplayName("Non-Slivers do not gain Frenetic Sliver's ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new FreneticSliver());
        harness.addToBattlefield(player1, new GossamerPhantasm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
