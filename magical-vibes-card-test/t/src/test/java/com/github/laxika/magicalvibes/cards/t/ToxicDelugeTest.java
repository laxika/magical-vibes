package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantTortoise;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.model.GameStatus;
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

@CardUsed({ToxicDeluge.class, GrizzlyBears.class, GiantTortoise.class, Counterspell.class})
class ToxicDelugeTest extends BaseCardTest {

    @Test
    @DisplayName("Pays X life and gives all creatures -X/-X")
    void paysLifeAndShrinksAllCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertLife(player1, 18);
        assertThat(ownBear.getPowerModifier()).isEqualTo(-2);
        assertThat(ownBear.getToughnessModifier()).isEqualTo(-2);
        assertThat(opposingBear.getPowerModifier()).isEqualTo(-2);
        assertThat(opposingBear.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("X=0 pays no life and leaves creatures unchanged")
    void zeroXPaysNoLifeAndDoesNotShrinkCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast when X life cannot be paid")
    void cannotPayMoreLifeThanAvailable() {
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("Life is paid when casting and is not refunded if countered")
    void counteringDoesNotRefundLifeOrShrinkCreatures() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player1, new GiantTortoise());
        ToxicDeluge deluge = new ToxicDeluge();
        harness.setHand(player1, List.of(deluge));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 3);

        harness.assertLife(player1, 17);
        assertThat(tortoise.getToughnessModifier()).isZero();

        harness.castInstant(player2, 0, deluge.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Toxic Deluge");
        harness.assertOnBattlefield(player1, "Giant Tortoise");
        assertThat(tortoise.getPowerModifier()).isZero();
        assertThat(tortoise.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Creatures with zero toughness die on both sides")
    void killsCreaturesWithZeroToughness() {
        harness.addToBattlefield(player1, new GiantTortoise());
        harness.addToBattlefield(player2, new GiantTortoise());
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 4);

        harness.assertNotOnBattlefield(player1, "Giant Tortoise");
        harness.assertNotOnBattlefield(player2, "Giant Tortoise");
        harness.assertInGraveyard(player1, "Giant Tortoise");
        harness.assertInGraveyard(player2, "Giant Tortoise");
    }

    @Test
    @DisplayName("Surviving creatures recover at cleanup and later creatures are unaffected")
    void reductionExpiresAndDoesNotAffectLaterCreatures() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player1, new GiantTortoise());
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gqs.getEffectivePower(gd, tortoise)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(2);
        Permanent laterTortoise = harness.enterBattlefieldAndReturn(player2, new GiantTortoise());
        assertThat(laterTortoise.getPowerModifier()).isZero();
        assertThat(laterTortoise.getToughnessModifier()).isZero();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(tortoise.getPowerModifier()).isZero();
        assertThat(tortoise.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, tortoise)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only the fixed mana cost is paid even when X is large")
    void largeXDoesNotIncreaseManaCost() {
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 10);

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Toxic Deluge");
    }

    @Test
    @DisplayName("Paying all remaining life is legal but loses the game before resolution")
    void canPayEntireLifeTotalButLosesBeforeResolution() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player2, new GiantTortoise());
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.setLife(player1, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 3);

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(tortoise.getPowerModifier()).isZero();
        assertThat(tortoise.getToughnessModifier()).isZero();
    }
}
