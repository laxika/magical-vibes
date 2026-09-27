package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TideShaper.class, Island.class, Mountain.class, GrizzlyBears.class})
class TideShaperTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, Tide Shaper does not change a land")
    void withoutKickerDoesNotChangeLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        castTideShaper(false, null);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("When kicked, Tide Shaper makes the target land an Island until it leaves")
    void kickedChangesLandUntilSourceLeaves() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent tideShaper = castTideShaper(true, mountain);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.ISLAND);

        gd.playerBattlefields.get(player1.getId()).remove(tideShaper);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Kicker ETB cannot target a nonland permanent")
    void kickerCannotTargetNonland() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TideShaper()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Tide Shaper gets +1/+1 while an opponent controls an Island")
    void boostedWhileOpponentControlsIsland() {
        harness.addToBattlefield(player1, new TideShaper());

        assertThat(stats(player1)).containsExactly(1, 1);

        harness.addToBattlefield(player2, new Island());

        assertThat(stats(player1)).containsExactly(2, 2);
    }

    private Permanent castTideShaper(boolean kicked, Permanent target) {
        harness.setHand(player1, List.of(new TideShaper()));
        harness.addMana(player1, ManaColor.BLUE, kicked ? 2 : 1);

        if (kicked) {
            harness.castKickedCreature(player1, 0, target.getId());
        } else {
            harness.castCreature(player1, 0);
        }
        harness.passBothPriorities();
        if (kicked) {
            harness.passBothPriorities();
        }

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Tide Shaper"))
                .findFirst()
                .orElseThrow();
    }

    private List<Integer> stats(com.github.laxika.magicalvibes.model.Player player) {
        Permanent tideShaper = gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Tide Shaper"))
                .findFirst()
                .orElseThrow();
        return List.of(gqs.getEffectivePower(gd, tideShaper), gqs.getEffectiveToughness(gd, tideShaper));
    }
}
