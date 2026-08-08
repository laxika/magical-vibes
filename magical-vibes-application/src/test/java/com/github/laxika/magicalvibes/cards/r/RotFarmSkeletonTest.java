package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RotFarmSkeletonTest extends BaseCardTest {

    private void mainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void payMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Can't block")
    void cantBlock() {
        Permanent skeleton = new Permanent(new RotFarmSkeleton());
        gd.playerBattlefields.get(player1.getId()).add(skeleton);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        assertThat(bls.canBlockAttacker(gd, skeleton, bears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Graveyard ability mills four and returns the Skeleton to the battlefield")
    void graveyardAbilityReturnsSkeleton() {
        mainPhase();
        harness.setGraveyard(player1, List.of(new RotFarmSkeleton()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        payMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertNotInGraveyard(player1, "Rot Farm Skeleton");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rot Farm Skeleton"))).hasSize(1);
    }

    @Test
    @DisplayName("Can't activate with fewer than four cards in library (CR 701.17b)")
    void cannotActivateWithSmallLibrary() {
        mainPhase();
        harness.setGraveyard(player1, List.of(new RotFarmSkeleton()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        payMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mill");

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Rot Farm Skeleton");
    }

    @Test
    @DisplayName("Can't activate outside a main phase (sorcery speed only)")
    void cannotActivateAtInstantSpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new RotFarmSkeleton()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        payMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }
}
