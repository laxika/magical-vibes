package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClawsOut.class, SavannahLions.class, BearCub.class})
class ClawsOutTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast with insufficient mana when no Cats are controlled")
    void cannotCastWithoutCats() {
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity for Cats reduces the cost for each Cat controlled")
    void affinityReducesCostForEachCatControlled() {
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(1);

        harness.castInstant(player1, 0, (UUID) null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cats controlled by an opponent do not reduce the cost")
    void opponentCatsDoNotReduceCost() {
        harness.addToBattlefield(player2, new SavannahLions());
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Boosts your creatures but not your opponent's creatures")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new BearCub());
        harness.addToBattlefield(player2, new BearCub());
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(permanentOf(player1, "Bear Cub").getEffectivePower()).isEqualTo(4);
        assertThat(permanentOf(player1, "Bear Cub").getEffectiveToughness()).isEqualTo(4);
        assertThat(permanentOf(player2, "Bear Cub").getEffectivePower()).isEqualTo(2);
        assertThat(permanentOf(player2, "Bear Cub").getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new BearCub());
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(2);

        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = permanentOf(player1, "Bear Cub");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void excessCatsReduceCostToTwoWhiteMana() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new SavannahLions());
        }
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(0);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(cat -> {
                    assertThat(cat.getEffectivePower()).isEqualTo(4);
                    assertThat(cat.getEffectiveToughness()).isEqualTo(3);
                });
    }

    @Test
    void affinityDoesNotReduceWhiteManaRequirement() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new SavannahLions());
        }
        harness.setHand(player1, List.of(new ClawsOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void nonCatsDoNotReduceCost() {
        harness.addToBattlefield(player1, new BearCub());
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void boostsCreaturesPresentAtResolutionButNotLaterEntrants() {
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(3);
        harness.castInstant(player1, 0, (UUID) null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new BearCub());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BearCub());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(4);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(4);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Claws Out");
    }

    @Test
    void canResolveWithoutCreatures() {
        harness.setHand(player1, List.of(new ClawsOut()));
        addClawsOutMana(3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Claws Out");
    }

    private void addClawsOutMana(int colorless) {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }

    private Permanent permanentOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
