package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeadshotTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the first target and it deals damage equal to its power to the second target")
    void tapsFirstTargetAndDealsPowerDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(bears.getId(), harness.getPermanentId(player2, "Llanowar Elves")));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Target survives when the tapped creature's power is below its toughness")
    void targetSurvivesInsufficientDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(bears.getId(), harness.getPermanentId(player2, "Air Elemental")));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Both targets may be creatures the caster controls")
    void mayTapOwnCreatureToShootOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(elemental.getId(), harness.getPermanentId(player1, "Llanowar Elves")));
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void cannotChooseSameCreatureTwice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No damage is dealt when the tapped creature leaves before resolution")
    void noDamageWhenShooterRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(bears.getId(), harness.getPermanentId(player2, "Llanowar Elves")));
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }
}
