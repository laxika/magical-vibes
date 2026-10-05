package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FlintGolem;
import com.github.laxika.magicalvibes.cards.s.StrongholdZeppelin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PredatorFlagship.class, FlintGolem.class, StrongholdZeppelin.class})
class PredatorFlagshipTest extends BaseCardTest {

    @Test
    @DisplayName("Two-mana ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        addFlagship();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlintGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Five-mana ability destroys a creature with flying")
    void destroysCreatureWithFlying() {
        Permanent flagship = addFlagship();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StrongholdZeppelin());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(flagship.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Stronghold Zeppelin");
    }

    @Test
    @DisplayName("Destroy ability cannot target a creature without flying")
    void cannotDestroyCreatureWithoutFlying() {
        addFlagship();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlintGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }

    @Test
    @DisplayName("A creature granted flying becomes eligible for destruction")
    void grantedFlyingMakesCreatureEligibleForDestruction() {
        Permanent flagship = addFlagship();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlintGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(flagship.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Flint Golem");
    }

    @Test
    @DisplayName("Flying ability works repeatedly while the Flagship is tapped")
    void grantsFlyingRepeatedlyWhileTapped() {
        Permanent flagship = addFlagship();
        flagship.setTapped(true);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FlintGolem());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new FlintGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isTrue();
        assertThat(flagship.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Destroy ability works on the turn the noncreature artifact enters")
    void destroysCreatureImmediatelyAfterEntering() {
        Permanent flagship = harness.enterBattlefieldAndReturn(player1, new PredatorFlagship());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StrongholdZeppelin());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(flagship.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Stronghold Zeppelin");
        harness.assertInGraveyard(player1, "Stronghold Zeppelin");
    }

    @Test
    @DisplayName("Destroy ability cannot be activated while the Flagship is tapped")
    void cannotDestroyWhileTapped() {
        Permanent flagship = addFlagship();
        flagship.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StrongholdZeppelin());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player2, "Stronghold Zeppelin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying ability requires two mana")
    void cannotGrantFlyingWithInsufficientMana() {
        addFlagship();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlintGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroy ability requires five mana")
    void cannotDestroyWithInsufficientMana() {
        Permanent flagship = addFlagship();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StrongholdZeppelin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(flagship.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Stronghold Zeppelin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying ability cannot target the noncreature Flagship")
    void cannotGrantFlyingToNoncreature() {
        Permanent flagship = addFlagship();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flagship.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.hasKeyword(gd, flagship, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Temporary flying does not remove a creature's printed flying at cleanup")
    void printedFlyingSurvivesCleanup() {
        addFlagship();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StrongholdZeppelin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    private Permanent addFlagship() {
        Permanent flagship = harness.addToBattlefieldAndReturn(player1, new PredatorFlagship());
        flagship.setSummoningSick(false);
        return flagship;
    }
}
