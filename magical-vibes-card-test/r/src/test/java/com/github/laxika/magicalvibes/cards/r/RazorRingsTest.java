package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
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

@CardUsed({RazorRings.class, GrizzlyBears.class, HealingSalve.class, WallOfSwords.class})
class RazorRingsTest extends BaseCardTest {

    @Test
    @DisplayName("deals 4 damage and gains life equal to excess damage")
    void gainsLifeForExcessDamage() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);

        castRazorRings(target);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("gains no life when there is no excess damage")
    void gainsNoLifeWithoutExcessDamage() {
        harness.forceActivePlayer(player1);
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setToughness(5);
        Permanent target = addCreatureReady(player2, targetCard);
        target.setBlocking(true);

        castRazorRings(target);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("counts only damage that was not prevented")
    void gainsLifeOnlyForDamageActuallyDealt() {
        harness.forceActivePlayer(player1);
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setToughness(5);
        Permanent target = addCreatureReady(player2, targetCard);
        target.setBlocking(true);

        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        castRazorRings(target);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("cannot target a creature that is neither attacking nor blocking")
    void rejectsIdleCreature() {
        harness.forceActivePlayer(player1);
        Permanent idle = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RazorRings()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking or blocking creature");
    }

    @Test
    @DisplayName("includes damage already marked when determining excess damage")
    void gainsLifeForDamageBeyondRemainingToughness() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new WallOfSwords());
        target.setBlocking(true);
        target.setMarkedDamage(3);

        castRazorRings(target);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Wall of Swords");
    }

    @Test
    @DisplayName("gains no life when the damage is exactly lethal")
    void gainsNoLifeForExactlyLethalDamage() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new WallOfSwords());
        target.setBlocking(true);
        target.setMarkedDamage(1);

        castRazorRings(target);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Wall of Swords");
    }

    @Test
    @DisplayName("partial prevention removes excess damage against a small creature")
    void preventionCanEliminateExcessDamage() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setBlocking(true);

        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        castRazorRings(target);

        harness.assertLife(player1, 20);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("can target your own blocking creature and gains life for its excess damage")
    void canTargetOwnBlockingCreature() {
        harness.forceActivePlayer(player2);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setBlocking(true);

        castRazorRings(target);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("does not deal damage or gain life if the creature stops attacking before resolution")
    void fizzlesWhenTargetLeavesCombat() {
        harness.forceActivePlayer(player2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);

        harness.setHand(player1, List.of(new RazorRings()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Razor Rings");
    }

    private void castRazorRings(Permanent target) {
        harness.setHand(player1, List.of(new RazorRings()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
