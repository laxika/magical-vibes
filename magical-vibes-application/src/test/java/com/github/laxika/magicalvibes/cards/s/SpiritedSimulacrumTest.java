package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritedSimulacrum.class, Forest.class, GrizzlyBears.class, WrathOfGod.class})
class SpiritedSimulacrumTest extends BaseCardTest {

    @Test
    void entersWithASeekingLandTapped() {
        Forest land = new Forest();
        GrizzlyBears otherLibraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(otherLibraryCard, land));

        harness.enterBattlefieldAndReturn(player1, new SpiritedSimulacrum());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLibraryCard);
    }

    @Test
    void seeksANonlandCardWhenItLeavesTheBattlefield() {
        Forest land = new Forest();
        GrizzlyBears soughtCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, soughtCard));
        harness.addToBattlefield(player1, new SpiritedSimulacrum());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(soughtCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SpiritedSimulacrum);
    }

    @Test
    void canBeCastForItsWarpCost() {
        harness.setHand(player1, List.of(new SpiritedSimulacrum()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent simulacrum = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SpiritedSimulacrum)
                .findFirst()
                .orElseThrow();
        assertThat(simulacrum.isCastWithWarp()).isTrue();
    }
}
