package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
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

@CardUsed({ViciousOffering.class, GrizzlyBears.class, HillGiant.class, PrimordialWurm.class})
class ViciousOfferingTest extends BaseCardTest {

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Without kicker — gives -2/-2, kills a 2/2 creature")
    void unkickedKills2Toughness() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Grizzly Bears is 2/2, -2/-2 makes it 0/0 → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without kicker — gives -2/-2, 3/3 creature survives")
    void unkickedDoesNotKill3Toughness() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Hill Giant is 3/3, -2/-2 makes it 1/1 → survives
        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getToughnessModifier()).isEqualTo(-2);
        assertThat(giant.getPowerModifier()).isEqualTo(-2);
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("With kicker — gives -5/-5, kills a 3/3 creature")
    void kickedKills3Toughness() {
        // Need a creature to sacrifice for kicker cost
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, targetId, sacrificeId);
        harness.passBothPriorities();

        // Hill Giant is 3/3, -5/-5 makes it -2/-2 → dies
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        // Sacrificed creature should also be gone
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With kicker — sacrificed creature goes to graveyard")
    void kickedSacrificesCreature() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, targetId, sacrificeId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    // ===== Spell goes to graveyard =====

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vicious Offering");
    }

    @Test
    @DisplayName("Unkicked Offering can target your own creature and wears off at cleanup")
    void unkickedReductionEndsAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Kicked Offering replaces -2/-2 with exactly -5/-5 until cleanup")
    void kickedReductionReplacesBaseEffectAndEndsAtCleanup() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Primordial Wurm");
        harness.assertOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Primordial Wurm");
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The targeted creature may be sacrificed to pay kicker")
    void canSacrificeTargetToPayKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), target.getId());

        harness.assertNotOnBattlefield(player1, "Primordial Wurm");
        harness.assertInGraveyard(player1, "Primordial Wurm");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vicious Offering");
    }

    @Test
    @DisplayName("Kicker cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Primordial Wurm");
        harness.assertInHand(player1, "Vicious Offering");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kicker requires a creature sacrifice")
    void cannotKickWithoutSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new ViciousOffering()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Primordial Wurm");
        harness.assertInHand(player1, "Vicious Offering");
        assertThat(gd.stack).isEmpty();
    }
}
