package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WorthyCause.class, HornedTurtle.class, LowlandGiant.class})
class WorthyCauseTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the sacrificed creature's current toughness, not its power")
    void gainsLifeEqualToToughness() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        turtle.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new WorthyCause()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int startingLife = gd.playerLifeTotals.get(player1.getId());
        UUID turtleId = turtle.getId();
        harness.castInstantWithSacrifice(player1, 0, null, turtleId);

        // The sacrifice is a cost, so the creature is gone before the spell resolves.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(turtleId));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 5);
    }

    @Test
    @DisplayName("Without buyback the spell goes to the graveyard as it resolves")
    void resolvesToGraveyardWithoutBuyback() {
        harness.addToBattlefield(player1, new LowlandGiant());
        harness.setHand(player1, List.of(new WorthyCause()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castInstantWithSacrifice(player1, 0, null, harness.getPermanentId(player1, "Lowland Giant"));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 3);
        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).contains("Worthy Cause");
    }

    @Test
    @DisplayName("Paying buyback returns the spell to hand as it resolves")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player1, new LowlandGiant());
        harness.setHand(player1, List.of(new WorthyCause()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castInstantWithSacrificeAndBuyback(player1, 0, null,
                harness.getPermanentId(player1, "Lowland Giant"));

        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 3);
        assertThat(handNames(player1)).containsExactly("Worthy Cause");
        assertThat(graveyardNames(player1)).doesNotContain("Worthy Cause");
    }

    @Test
    @DisplayName("Cannot be cast without a creature to sacrifice")
    void cannotCastWithoutCreature() {
        harness.setHand(player1, List.of(new WorthyCause()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
