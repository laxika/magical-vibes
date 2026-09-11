package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamiteBlessing.class, GrizzlyBears.class, ProdigalPyromancer.class})
class SamiteBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can activate Samite Blessing's prevention ability")
    void enchantedCreatureActivatesPreventionAbility() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        attachBlessing(enchanted);
        Permanent protectedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent source = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, enchanted), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, source.getId());

        harness.activateAbility(player2, indexOf(player2, source), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Damage to another creature does not consume Samite Blessing's shield")
    void damageToAnotherCreatureDoesNotConsumeShield() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        attachBlessing(enchanted);
        Permanent protectedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent source = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, enchanted), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.activateAbility(player2, indexOf(player2, source), null, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(otherCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);

        source.untap();
        harness.activateAbility(player2, indexOf(player2, source), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from another source is not prevented")
    void damageFromAnotherSourceIsNotPrevented() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        attachBlessing(enchanted);
        Permanent protectedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosenSource = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent otherSource = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, enchanted), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        harness.activateAbility(player2, indexOf(player2, otherSource), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);
    }

    @Test
    @DisplayName("Samite Blessing's ability cannot target a player")
    void abilityCannotTargetPlayer() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        attachBlessing(enchanted);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, enchanted), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Samite Blessing's prevention shield expires at the end of the turn")
    void shieldExpiresAtEndOfTurn() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        attachBlessing(enchanted);
        Permanent protectedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent source = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, enchanted), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    private void attachBlessing(Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SamiteBlessing());
        aura.setAttachedTo(enchanted.getId());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
