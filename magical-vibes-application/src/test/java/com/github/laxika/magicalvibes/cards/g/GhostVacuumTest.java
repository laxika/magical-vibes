package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostVacuum.class, GrizzlyBears.class, Shock.class})
class GhostVacuumTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability exiles a target card from any graveyard and tracks it")
    void tapAbilityExilesTargetCardFromGraveyard() {
        Permanent vacuum = harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(vacuum.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Sacrifice ability returns tracked creatures as 1/1 Spirits with flying counters")
    void sacrificeAbilityReturnsTrackedCreaturesWithSpiritCharacteristics() {
        Permanent vacuum = harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(bears, shock));

        harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        vacuum.untap();
        harness.activateAbility(player1, 0, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        vacuum.untap();

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(gd.getCardsExiledByPermanent(vacuum.getId())).containsExactly(shock);
        harness.assertInGraveyard(player1, "Ghost Vacuum");
    }

    @Test
    @DisplayName("Tap ability cannot target a card outside a graveyard")
    void tapAbilityRejectsNonGraveyardTarget() {
        harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsAllCreaturesFromBothOwnersButOnlyFromThisVacuum() {
        Permanent vacuum = harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        Permanent otherVacuum = harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        GrizzlyBears ownBears = new GrizzlyBears();
        GrizzlyBears opposingBears = new GrizzlyBears();
        GrizzlyBears otherBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownBears));
        harness.setGraveyard(player2, List.of(opposingBears, otherBears));

        harness.activateAbility(player1, 0, null, ownBears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        vacuum.untap();
        harness.activateAbility(player1, 0, null, opposingBears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, otherBears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        vacuum.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Ghost Vacuum");
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2).allSatisfy(returned -> {
            assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
            assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
            assertThat(returned.isTapped()).isFalse();
        });
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(vacuum.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(otherVacuum.getId())).containsExactly(otherBears);
    }

    @Test
    void competingVacuumMakesGraveyardTargetIllegalBeforeResolution() {
        Permanent vacuum = harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        Permanent otherVacuum = harness.addToBattlefieldAndReturn(player2, new GhostVacuum());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player2, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(vacuum.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(otherVacuum.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void sacrificeAbilityCanResolveWithoutExiledCreatures() {
        harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost Vacuum");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeAbilityRejectsOpponentTurn() {
        Permanent vacuum = harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vacuum.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ghost Vacuum");
    }

    @Test
    void sacrificeAbilityRejectsNonMainPhase() {
        harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Ghost Vacuum");
    }

    @Test
    void sacrificeAbilityRejectsNonemptyStack() {
        Permanent vacuum = harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD);
        vacuum.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Ghost Vacuum");
    }

    @Test
    void sacrificeAbilityRequiresSixManaAndUntappedSource() {
        Permanent vacuum = harness.addToBattlefieldAndReturn(player1, new GhostVacuum());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Ghost Vacuum");
        assertThat(vacuum.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        vacuum.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Ghost Vacuum");
    }
}
