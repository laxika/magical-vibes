package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NoxiousRevival;
import com.github.laxika.magicalvibes.cards.t.ThoughtScour;
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

@CardUsed({RotFarmSkeleton.class, Forest.class, GrizzlyBears.class, NoxiousRevival.class, ThoughtScour.class})
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
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new RotFarmSkeleton());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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

    @Test
    @DisplayName("Milling is paid immediately and only the activating Skeleton returns")
    void millCostIsPaidBeforeResolutionAndReturnsOnlySource() {
        mainPhase();
        RotFarmSkeleton source = new RotFarmSkeleton();
        RotFarmSkeleton other = new RotFarmSkeleton();
        harness.setGraveyard(player1, List.of(source));
        harness.setLibrary(player1, List.of(other, new Forest(), new Forest(), new Forest()));
        payMana();

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source, other).hasSize(5);
        harness.assertNotOnBattlefield(player1, "Rot Farm Skeleton");

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(source).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(source.getId()));
    }

    @Test
    @DisplayName("Can't activate during an opponent's main phase")
    void cannotActivateDuringOpponentsMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new RotFarmSkeleton()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        payMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Rot Farm Skeleton");
    }

    @Test
    @DisplayName("The old ability cannot return a Skeleton that left and reentered the graveyard")
    void cannotReturnNewGraveyardObject() {
        mainPhase();
        RotFarmSkeleton source = new RotFarmSkeleton();
        harness.setGraveyard(player1, List.of(source));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new NoxiousRevival(), new ThoughtScour()));
        payMana();
        harness.activateGraveyardAbility(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertNotInGraveyard(player1, "Rot Farm Skeleton");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertInGraveyard(player1, "Rot Farm Skeleton");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rot Farm Skeleton");
        harness.assertInGraveyard(player1, "Rot Farm Skeleton");
    }
}
