package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({Ramroller.class, Ornithopter.class, GrizzlyBears.class})
class RamrollerTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/3 with no other artifact")
    void noBoostAlone() {
        harness.addToBattlefield(player1, new Ramroller());

        Permanent ramroller = findPermanent(player1, "Ramroller");
        assertThat(gqs.getEffectivePower(gd, ramroller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ramroller)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +2/+0 (becomes 4/3) while controlling another artifact")
    void boostWithAnotherArtifact() {
        harness.addToBattlefield(player1, new Ramroller());
        harness.addToBattlefield(player1, new Ornithopter());

        Permanent ramroller = findPermanent(player1, "Ramroller");
        assertThat(gqs.getEffectivePower(gd, ramroller)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ramroller)).isEqualTo(3);
    }

    @Test
    @DisplayName("A nonartifact permanent does not grant the boost")
    void noBoostWithNonArtifact() {
        harness.addToBattlefield(player1, new Ramroller());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent ramroller = findPermanent(player1, "Ramroller");
        assertThat(gqs.getEffectivePower(gd, ramroller)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's artifact does not grant the boost")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new Ramroller());
        harness.addToBattlefield(player2, new Ornithopter());

        Permanent ramroller = findPermanent(player1, "Ramroller");
        assertThat(gqs.getEffectivePower(gd, ramroller)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Ramrollers each count as the other's artifact")
    void twoRamrollersBoostEachOther() {
        harness.addToBattlefield(player1, new Ramroller());
        harness.addToBattlefield(player1, new Ramroller());

        for (Permanent ramroller : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.getEffectivePower(gd, ramroller)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Loses the boost when the other artifact leaves the battlefield")
    void losesBoostWhenArtifactLeaves() {
        harness.addToBattlefield(player1, new Ramroller());
        harness.addToBattlefield(player1, new Ornithopter());

        Permanent ramroller = findPermanent(player1, "Ramroller");
        assertThat(gqs.getEffectivePower(gd, ramroller)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Ornithopter"));

        assertThat(gqs.getEffectivePower(gd, ramroller)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declaring no attackers while Ramroller can attack throws 'must attack'")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new Ramroller());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A tapped Ramroller is not required to attack")
    void tappedRamrollerNeedNotAttack() {
        Permanent ramroller = addCreatureReady(player1, new Ramroller());
        ramroller.setTapped(true);

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A summoning-sick Ramroller is not required to attack")
    void summoningSickRamrollerNeedNotAttack() {
        harness.addToBattlefield(player1, new Ramroller());

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Declaring Ramroller as an attacker satisfies its requirement")
    void attackingSatisfiesRequirement() {
        Permanent ramroller = addCreatureReady(player1, new Ramroller());

        assertThatCode(() -> declareAttackersAndPrepareBlockers(List.of(0))).doesNotThrowAnyException();
        assertThat(ramroller.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Multiple other artifacts grant only one +2/+0 bonus")
    void boostDoesNotStackForMultipleArtifacts() {
        harness.addToBattlefield(player1, new Ramroller());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        Permanent ramroller = findPermanent(player1, "Ramroller");
        assertThat(gqs.getEffectivePower(gd, ramroller)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ramroller)).isEqualTo(3);
    }
}
