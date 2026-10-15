package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CrabappleCohort;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Valleymaker.class, CrabappleCohort.class, Forest.class, Mountain.class})
class ValleymakerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Mountain deals 3 damage to target creature")
    void mountainAbilityDealsThreeDamage() {
        addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new CrabappleCohort());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Crabapple Cohort");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Crabapple Cohort");
        assertThat(target).isNotNull();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Mountain ability cannot target a non-creature")
    void mountainAbilityRejectsNonCreatureTarget() {
        addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forest ability adds {G}{G}{G} to the chosen controller's mana pool")
    void forestAbilityAddsManaToController() {
        addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        // Mana ability: no stack, pauses only to choose the recipient player.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Forest ability can add {G}{G}{G} to a chosen opponent's mana pool")
    void forestAbilityAddsManaToOpponent() {
        addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    void mountainAbilityPaysCostsBeforeResolvingAndCanTargetItself() {
        Permanent source = addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Mountain());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, source.getId());

        assertThat(source.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Valleymaker");
    }

    @Test
    void mountainAbilityCannotSacrificeOpponentsMountainOrOwnForest() {
        Permanent source = addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void forestAbilityCannotSacrificeOpponentsForestOrOwnMountain() {
        Permanent source = addCreatureReady(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void summoningSicknessPreventsBothAbilities() {
        harness.addToBattlefield(player1, new Valleymaker());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID sourceId = harness.getPermanentId(player1, "Valleymaker");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, sourceId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedValleymakerCannotActivateEitherAbility() {
        Permanent source = addCreatureReady(player1, new Valleymaker());
        source.tap();
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void forestAbilityCanSacrificeTappedForestAndResolvesWithoutUsingStack() {
        Permanent source = addCreatureReady(player1, new Valleymaker());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(source.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();

        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mountainAbilityAllowsChoosingWhichMountainToSacrifice() {
        Permanent source = addCreatureReady(player1, new Valleymaker());
        Permanent firstMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        secondMountain.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.handlePermanentChosen(player1, secondMountain.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstMountain)
                .doesNotContain(secondMountain);
        harness.assertInGraveyard(player1, "Mountain");

        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isEqualTo(3);
    }
}
