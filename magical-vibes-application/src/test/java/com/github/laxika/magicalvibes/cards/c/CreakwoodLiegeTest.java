package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreakwoodLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other black creatures you control")
    void buffsOtherBlack() {
        harness.addToBattlefield(player1, new CreakwoodLiege());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent black = findPermanent(player1, "Black Knight");

        // 2/2 base + 1/1 = 3/3
        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(3);
    }

    @Test
    @DisplayName("Buffs other green creatures you control")
    void buffsOtherGreen() {
        harness.addToBattlefield(player1, new CreakwoodLiege());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent green = findPermanent(player1, "Grizzly Bears");

        // 2/2 base + 1/1 = 3/3
        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff itself or off-color creatures")
    void doesNotBuffItselfOrOffColor() {
        harness.addToBattlefield(player1, new CreakwoodLiege());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent red = findPermanent(player1, "Hill Giant");

        // Red creature is neither black nor green.
        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(3);
        // The Liege's own "other" boosts do not apply to itself; token check below covers it too.
    }

    @Test
    @DisplayName("Upkeep trigger may create a 1/1 black and green Worm token")
    void upkeepCreatesWormToken() {
        harness.addToBattlefield(player1, new CreakwoodLiege());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to UPKEEP
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countWormTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger creates no token")
    void upkeepDeclinedCreatesNoToken() {
        harness.addToBattlefield(player1, new CreakwoodLiege());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to UPKEEP
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countWormTokens(player1)).isZero();
    }

    @Test
    @DisplayName("The created Worm token is black and green so both anthems apply")
    void wormTokenIsBuffedByBothAnthems() {
        harness.addToBattlefield(player1, new CreakwoodLiege());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent worm = findPermanent(player1, "Worm");
        // 1/1 base + 1/1 (black anthem) + 1/1 (green anthem) = 3/3
        assertThat(gqs.getEffectivePower(gd, worm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, worm)).isEqualTo(3);
    }

    private int countWormTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Worm"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.WORM))
                .count();
    }
}
