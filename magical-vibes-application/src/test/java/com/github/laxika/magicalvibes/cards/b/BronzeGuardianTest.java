package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BronzeGuardian.class, Ornithopter.class, Shock.class})
class BronzeGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of artifacts you control; toughness stays 5")
    void powerEqualsControlledArtifacts() {
        Permanent guardian = addGuardian(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);

        harness.addToBattlefield(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ward {2} is granted to other artifacts you control")
    void otherControlledArtifactsHaveWard() {
        addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShockAt(player2, artifact, 1);

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward {2} can be paid for on another controlled artifact")
    void otherControlledArtifactWardCanBePaid() {
        addGuardian(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShockAt(player2, artifact, 3);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(artifact.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward is not granted to an opponent's artifact")
    void opponentArtifactDoesNotHaveWard() {
        addGuardian(player1);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castShockAt(player1, opponentArtifact, 1);

        harness.assertInGraveyard(player1, "Shock");
        assertThat(opponentArtifact.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addGuardian(Player player) {
        return addCreatureReady(player, new BronzeGuardian());
    }

    private void castShockAt(Player caster, Permanent target, int mana) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        if (mana > 1) {
            harness.addMana(caster, ManaColor.COLORLESS, mana - 1);
        }

        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
