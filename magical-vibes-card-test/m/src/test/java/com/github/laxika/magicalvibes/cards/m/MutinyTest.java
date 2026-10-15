package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mutiny.class, HillGiant.class, GrizzlyBears.class})
class MutinyTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creature deals its power to another creature that player controls")
    void opponentCreatureDealsPowerDamageToAnotherCreature() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        UUID victimId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Mutiny()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(sourceId, victimId));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The second target must be controlled by the first target's controller")
    void rejectsSecondTargetControlledByAnotherPlayer() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        UUID victimId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Mutiny()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(sourceId, victimId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsSameCreatureForBothTargets() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new Mutiny()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(sourceId, sourceId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsSourceControlledByCaster() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        UUID victimId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Mutiny()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(sourceId, victimId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({MomentOfCraving.class})
    void usesSourcePowerAtResolutionAndDoesNotDealReciprocalDamage() {
        var source = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        var victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mutiny(), new MomentOfCraving()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, List.of(source.getId(), victim.getId()));
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed({MomentOfCraving.class})
    void dealsNoDamageWhenSourceLeavesBeforeResolution() {
        var source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Mutiny(), new MomentOfCraving()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, List.of(source.getId(), victim.getId()));
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Mutiny");
    }

    @Test
    @CardUsed({MomentOfCraving.class})
    void dealsNoDamageWhenVictimLeavesBeforeResolution() {
        var source = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        var victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mutiny(), new MomentOfCraving()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, List.of(source.getId(), victim.getId()));
        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Mutiny");
    }
}
