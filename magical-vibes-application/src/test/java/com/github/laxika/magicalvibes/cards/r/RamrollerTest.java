package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        addReadyRamroller(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private Permanent addReadyRamroller(Player player) {
        Permanent perm = new Permanent(new Ramroller());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
