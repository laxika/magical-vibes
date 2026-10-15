package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShovingMatch.class, CloudSprite.class, Island.class})
class ShovingMatchTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures gain the tap ability until end of turn")
    void allCreaturesGainTapAbility() {
        Permanent playerOneCreature = addCreatureReady(player1, new CloudSprite());
        Permanent playerTwoCreature = addCreatureReady(player2, new CloudSprite());

        castShovingMatch();

        harness.activateAbility(player1, 0, null, playerTwoCreature.getId());
        harness.passBothPriorities();

        assertThat(playerOneCreature.isTapped()).isTrue();
        assertThat(playerTwoCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after Shoving Match resolves do not gain the ability")
    void laterCreaturesDoNotGainAbility() {
        addCreatureReady(player1, new CloudSprite());
        castShovingMatch();

        Permanent laterCreature = addCreatureReady(player2, new CloudSprite());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, laterCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        addCreatureReady(player1, new CloudSprite());
        castShovingMatch();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The granted ability can target only creatures")
    void grantedAbilityCannotTargetNoncreature() {
        addCreatureReady(player1, new CloudSprite());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        castShovingMatch();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A creature controlled by either player can activate the granted ability")
    void creaturesOfEitherPlayerCanActivateGrantedAbility() {
        Permanent playerOneCreature = addCreatureReady(player1, new CloudSprite());
        Permanent playerTwoCreature = addCreatureReady(player2, new CloudSprite());
        castShovingMatch();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, playerOneCreature.getId());
        harness.passBothPriorities();

        assertThat(playerTwoCreature.isTapped()).isTrue();
        assertThat(playerOneCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted tap ability cannot be activated by a summoning-sick creature")
    void summoningSickCreatureCannotActivateGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudSprite());
        Permanent target = addCreatureReady(player2, new CloudSprite());
        castShovingMatch();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature can target itself with the granted ability")
    void grantedAbilityCanTargetItsSource() {
        Permanent creature = addCreatureReady(player1, new CloudSprite());
        castShovingMatch();

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping is paid immediately and the target is tapped only on resolution")
    void tapCostIsPaidBeforeResolutionAndPreventsReactivation() {
        Permanent creature = addCreatureReady(player1, new CloudSprite());
        Permanent target = addCreatureReady(player2, new CloudSprite());
        castShovingMatch();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An activated granted ability resolves even if its source leaves the battlefield")
    void grantedAbilityResolvesAfterSourceLeaves() {
        Permanent creature = addCreatureReady(player1, new CloudSprite());
        Permanent target = addCreatureReady(player2, new CloudSprite());
        castShovingMatch();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castShovingMatch() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ShovingMatch(), "{2}{U}");
        harness.passBothPriorities();
    }
}
