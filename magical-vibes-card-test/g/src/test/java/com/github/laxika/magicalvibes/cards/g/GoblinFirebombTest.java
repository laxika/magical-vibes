package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinFirebomb.class, GrizzlyBears.class, Island.class})
class GoblinFirebombTest extends BaseCardTest {

    @Test
    void sacrificesItselfOnActivationAndDestroysTargetCreatureOnResolution() {
        Permanent firebomb = harness.addToBattlefieldAndReturn(player1, new GoblinFirebomb());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firebomb);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firebomb.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void canDestroyALand() {
        Permanent firebomb = harness.addToBattlefieldAndReturn(player1, new GoblinFirebomb());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firebomb.getCard());
    }

    @Test
    void canBeCastDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castFromHand(player1, new GoblinFirebomb(), "{1}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Goblin Firebomb");
    }

    @Test
    void canDestroyAnotherArtifactControlledByItsController() {
        Permanent firebomb = harness.addToBattlefieldAndReturn(player1, new GoblinFirebomb());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoblinFirebomb());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target).doesNotContain(firebomb);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firebomb.getCard(), target.getCard());
    }

    @Test
    void cannotActivateWithOnlySixMana() {
        Permanent firebomb = harness.addToBattlefieldAndReturn(player1, new GoblinFirebomb());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinFirebomb());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firebomb);
        assertThat(firebomb.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(firebomb.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent firebomb = harness.addToBattlefieldAndReturn(player1, new GoblinFirebomb());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinFirebomb());
        firebomb.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firebomb);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(firebomb.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItselfButAbilityDoesNotResolveAfterSacrifice() {
        Permanent firebomb = harness.addToBattlefieldAndReturn(player1, new GoblinFirebomb());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, firebomb.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firebomb);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firebomb.getCard());
    }
}
