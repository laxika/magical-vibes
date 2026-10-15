package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
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

@CardUsed({IrohsDemonstration.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class,
        Unsummon.class, WallOfAir.class})
class IrohsDemonstrationTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode damages each creature controlled by an opponent")
    void damagesOpponentsCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent largerOpponentCreature = addCreatureReady(player2, new HillGiant());
        harness.addToBattlefield(player2, new FountainOfYouth());

        cast(0, null);

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(largerOpponentCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The second mode deals 4 damage to a target creature")
    void damagesTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(1, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The second mode rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new IrohsDemonstration()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second mode deals exactly four damage and leaves other creatures unharmed")
    void dealsExactlyFourDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1, target.getId());

        harness.assertOnBattlefield(player2, "Wall of Air");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The second mode can target a creature you control")
    void canDamageOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(1, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The first mode can resolve with no opposing creatures")
    void firstModeNeedsNoCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(0, null);

        assertThat(own.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Iroh's Demonstration");
    }

    @Test
    @DisplayName("The second mode does not damage another creature when its target leaves")
    void targetLeavingDoesNotRedirectDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new IrohsDemonstration()));
        addMana();
        harness.castInstant(player1, 0, 1, target.getId());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(other.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Iroh's Demonstration");
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new IrohsDemonstration()));
        addMana();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
