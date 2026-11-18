package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AphettoAlchemist;
import com.github.laxika.magicalvibes.cards.g.GhosthelmCourier;
import com.github.laxika.magicalvibes.cards.p.PearlspearCourier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessBoostEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.s.SowerOfTemptation;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaeasLiege;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealityRipple.class, AngelicChorus.class, GrizzlyBears.class, Island.class, Millstone.class, Pacifism.class,
        SowerOfTemptation.class, GaeasLiege.class, Plains.class, Forest.class, GiantGrowth.class, GhosthelmCourier.class,
        AphettoAlchemist.class, PearlspearCourier.class})
class RealityRippleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving phases out the target creature")
    void phasesOutTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castRealityRipple(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("Resolving phases out the target artifact")
    void phasesOutTargetArtifact() {
        harness.addToBattlefield(player2, new Millstone());
        UUID targetId = harness.getPermanentId(player2, "Millstone");

        castRealityRipple(targetId);

        harness.assertNotOnBattlefield(player2, "Millstone");
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));
        harness.assertNotInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Resolving phases out the target land")
    void phasesOutTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");

        castRealityRipple(targetId);

        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));
        harness.assertNotInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("A phased-out permanent is not put into a graveyard — it returns during its controller's next untap step")
    void phasesBackInOnControllersNextUntapStep() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castRealityRipple(targetId);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");

        advanceTurn(); // player2's untap step — the creature phases in
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.phasedOutPermanents.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can phase out a permanent you control")
    void canPhaseOutOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        castRealityRipple(targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new RealityRipple()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    @DisplayName("A permanent stays phased out through the other player's untap step")
    void waitsForControllersUntapStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRealityRipple(creature.getId());
        harness.performUntapStep(player2);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.performUntapStep(player1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Phasing preserves counters and does not trigger entering the battlefield")
    void preservesCountersWithoutEnteringAgain() {
        harness.addToBattlefield(player2, new AngelicChorus());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.tap();
        harness.setLife(player2, 20);

        castRealityRipple(creature.getId());
        harness.runStateBasedActions();
        harness.performUntapStep(player2);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isEqualTo(creature.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.isTapped()).isFalse();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing Aura phases out and returns with its enchanted creature")
    void opposingAuraReturnsWithHost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        UUID auraId = harness.getPermanentId(player1, "Pacifism");

        castRealityRipple(creature.getId());
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
        harness.performUntapStep(player1);
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.performUntapStep(player2);

        harness.assertOnBattlefield(player1, "Pacifism");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(aura -> {
                    assertThat(aura.getId()).isEqualTo(auraId);
                    assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
                });
    }

    @Test
    @DisplayName("Phasing out Sower of Temptation ends its \"for as long as\" control effect for good")
    void phasingOutSowerEndsItsControlEffect() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SowerOfTemptation()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent sower = findPermanent(player1, "Sower of Temptation");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);

        castRealityRipple(sower.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        harness.performUntapStep(player1);
        harness.assertOnBattlefield(player1, "Sower of Temptation");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Phasing out Ghosthelm Courier ends its \"remains tapped\" boost, which doesn't return as it phases in")
    void phasingOutCourierEndsItsBoost() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new GhosthelmCourier());
        courier.setSummoningSick(false);
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new AphettoAlchemist());
        int basePower = gqs.getEffectivePower(gd, wizard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(courier), null,
                wizard.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(basePower + 2);

        castRealityRipple(courier.getId());

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isFalse();
        harness.performUntapStep(player2);
        harness.performUntapStep(player1);
        harness.assertOnBattlefield(player1, "Ghosthelm Courier");
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Pearlspear Courier's boost on itself, for as long as it remains tapped, ends as it phases out")
    void phasingOutCourierEndsItsBoostOnItself() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new PearlspearCourier());
        courier.setSummoningSick(false);
        int basePower = gqs.getEffectivePower(gd, courier);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(courier), null,
                courier.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, courier)).isEqualTo(basePower + 2);

        castRealityRipple(courier.getId());

        // Still tapped while phased out, so only the phasing out ends the boost.
        assertThat(courier.isTapped()).isTrue();
        assertThat(gd.floatingEffectsOnPermanent(courier.getId(), PowerToughnessBoostEffect.class)).isEmpty();
    }

    @Test
    @DisplayName("Phasing out Gaea's Liege doesn't end \"until it leaves the battlefield\": the land stays a Forest")
    void phasingOutGaeasLiegeKeepsTheForest() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new GaeasLiege());
        liege.setSummoningSick(false);
        harness.addToBattlefield(player1, new Forest());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(liege), null,
                plains.getId());
        harness.passBothPriorities();

        castRealityRipple(liege.getId());

        assertThat(gqs.effectiveBasicLandTypes(gd, plains)).containsExactly(CardSubtype.FOREST);
        harness.performUntapStep(player1);
        harness.assertOnBattlefield(player1, "Gaea's Liege");
        assertThat(gqs.effectiveBasicLandTypes(gd, plains)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("An \"until end of turn\" boost on a creature that phases out lasts until the cleanup step")
    void untilEndOfTurnBoostOutlastsPhasingOut() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        castRealityRipple(bears.getId());

        assertThat(gd.floatingEffectsOnPermanent(bears.getId(), PowerToughnessBoostEffect.class)).hasSize(1);
    }

    private void castRealityRipple(UUID targetId) {
        harness.setHand(player1, List.of(new RealityRipple()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
