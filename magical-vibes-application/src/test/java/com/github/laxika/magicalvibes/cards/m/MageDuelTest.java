package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MageDuel.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, Unsummon.class})
class MageDuelTest extends BaseCardTest {

    @Test
    void priorMageDuelReducesTheNextMageDuelCost() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MageDuel(), new MageDuel()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId(), elf.getId()));
        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId(), giant.getId()));

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void castingACreatureDoesNotReduceCost() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new LlanowarElves(), new MageDuel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bear.getId(), giant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsWithoutFightingWhenOpponentTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MageDuel()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(bear.getId(), giant.getId()));
        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Hill Giant");
    }

    @Test
    void doesNotBoostOrDamageOpponentWhenOwnTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MageDuel()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(bear.getId(), giant.getId()));
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mage Duel costs {2} less after casting an instant or sorcery")
    void costsLessAfterCastingInstantOrSorcery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GiantGrowth(), new MageDuel()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(bearId, giantId));

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Mage Duel cannot use the reduced cost without casting an instant or sorcery")
    void cannotUseReducedCostWithoutPriorInstantOrSorcery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new MageDuel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearId, giantId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mage Duel boosts your creature before it fights")
    void boostsBeforeFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new MageDuel()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, giantId));

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getMarkedDamage()).isEqualTo(3);
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Mage Duel requires a creature you control as its first target")
    void firstTargetMustBeControlledCreature() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MageDuel()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID opponentBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID ownElfId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentBearId, ownElfId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Mage Duel requires a creature you do not control as its second target")
    void secondTargetMustBeOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new MageDuel()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID ownElfId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownBearId, ownElfId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }
}
