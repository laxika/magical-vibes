package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimalMight.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Unsummon.class})
class PrimalMightTest extends BaseCardTest {

    @Test
    @DisplayName("X/X boost applies before the fight")
    void boostAppliesBeforeFight() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PrimalMight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, 3, List.of(elvesId, giantId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Hill Giant");
        Permanent elves = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(4);
    }

    @Test
    @DisplayName("The fight is optional")
    void fightCanBeSkipped() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new PrimalMight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, 2, elvesId);

        Permanent elves = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalMight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 3, bearId);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Targets must be a creature you control and a creature you don't control")
    void targetRestrictions() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalMight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID ownElvesId = harness.getPermanentId(player1, "Llanowar Elves");
        UUID opposingBearId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(opposingBearId, ownElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(ownBearId, ownElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }

    @Test
    @DisplayName("X can be zero and both creatures deal fight damage")
    void zeroXFightsWithoutBoost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 0, List.of(ownId, opposingId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing the opposing target still allows the boost but prevents fighting")
    void opposingTargetRemovedStillBoosts() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PrimalMight()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, 2, List.of(ownId, opposingId));
        harness.castAndResolveInstant(player2, 0, opposingId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Removing the friendly target prevents both boosting and fighting")
    void friendlyTargetRemovedDoesNotBoostOpponent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PrimalMight()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, 2, List.of(ownId, opposingId));
        harness.castAndResolveInstant(player2, 0, ownId);
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
