package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BlindingDrone;
import com.github.laxika.magicalvibes.cards.k.KozileksPathfinder;
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

@CardUsed({SpatialContortion.class, AirElemental.class, GrizzlyBears.class,
        BlindingDrone.class, KozileksPathfinder.class})
class SpatialContortionTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new SpatialContortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Gives target creature +3/-3 until end of turn")
    void appliesBoost() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        castOn(elemental);

        assertThat(elemental.getPowerModifier()).isEqualTo(3);
        assertThat(elemental.getToughnessModifier()).isEqualTo(-3);
        assertThat(elemental.getEffectivePower()).isEqualTo(7);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -3 toughness can kill a small creature")
    void killsSmallCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOn(bears);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The temporary modification wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castOn(elemental);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(0);
        assertThat(elemental.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SpatialContortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can modify an opponent's creature using colored mana for the generic cost")
    void modifiesOpposingCreatureWithMixedMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player1, List.of(new SpatialContortion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Kozilek's Pathfinder");
        harness.assertInGraveyard(player1, "Spatial Contortion");
    }

    @Test
    @DisplayName("A creature reduced to exactly zero toughness dies")
    void killsCreatureAtZeroToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindingDrone());

        castOn(target);

        harness.assertNotOnBattlefield(player2, "Blinding Drone");
        harness.assertInGraveyard(player2, "Blinding Drone");
    }

    @Test
    @DisplayName("Colored mana cannot pay the required colorless mana cost")
    void requiresColorlessMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksPathfinder());
        harness.setHand(player1, List.of(new SpatialContortion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Spatial Contortion");
        assertThat(gd.stack).isEmpty();
    }
}
