package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlaveringNulls.class, Swamp.class, Forest.class, GrizzlyBears.class})
class SlaveringNullsTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage with a Swamp lets its controller have the damaged player discard")
    void combatDamageWithSwampMayCauseDiscard() {
        addAttackingNulls(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card instanceof Forest);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Declining the may does not make the damaged player discard")
    void decliningMayDoesNotDiscard() {
        addAttackingNulls(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability does not trigger without a Swamp under its controller's control")
    void noTriggerWithoutControlledSwamp() {
        addAttackingNulls(player1);
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card instanceof Forest);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Losing the only Swamp before resolution prevents the discard")
    void losingSwampBeforeResolutionPreventsDiscard() {
        addAttackingNulls(player1);
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(swamp);
        gd.playerGraveyards.get(player1.getId()).add(swamp.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Gaining a Swamp after damage cannot create a missed trigger")
    void gainingSwampAfterDamageDoesNotTrigger() {
        addAttackingNulls(player1);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting the ability with an empty opposing hand completes without a discard choice")
    void emptyHandDoesNotRequireDiscardChoice() {
        addAttackingNulls(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingNulls(Player player) {
        Permanent nulls = addCreatureReady(player, new SlaveringNulls());
        nulls.setAttacking(true);
        return nulls;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
