package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnsnaringBridge.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class})
class EnsnaringBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Creature with power greater than controller's hand size cannot attack")
    void higherPowerCannotAttack() {
        harness.addToBattlefield(player1, new EnsnaringBridge());
        harness.setHand(player1, List.of(new LlanowarElves(), new LlanowarElves()));
        Permanent attacker = addCreatureReady(player1, new HillGiant());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature with power equal to controller's hand size can attack")
    void equalPowerCanAttack() {
        harness.addToBattlefield(player1, new EnsnaringBridge());
        harness.setHand(player1, List.of(new LlanowarElves(), new LlanowarElves()));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackers(player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Restriction uses the Bridge controller's hand size, not the attacker's controller")
    void restrictionUsesBridgeControllerHand() {
        // player1 controls the Bridge with an empty hand; player2 has a full hand but attacks.
        harness.addToBattlefield(player1, new EnsnaringBridge());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LlanowarElves(), new LlanowarElves(), new LlanowarElves(),
                new LlanowarElves(), new LlanowarElves(), new LlanowarElves(), new LlanowarElves()));
        Permanent attacker = addCreatureReady(player2, new LlanowarElves());

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restriction lifts once the controller's hand grows large enough")
    void largerHandLiftsRestriction() {
        harness.addToBattlefield(player1, new EnsnaringBridge());
        harness.setHand(player1, List.of(new LlanowarElves(), new LlanowarElves(), new LlanowarElves()));
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        harness.setLife(player2, 20);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackers(player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Restriction uses a creature's current power")
    void restrictionUsesEffectivePower() {
        harness.addToBattlefield(player1, new EnsnaringBridge());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.setHand(player1, List.of(new LlanowarElves(), new LlanowarElves(), new LlanowarElves(),
                new LlanowarElves()));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }
}
