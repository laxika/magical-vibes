package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SilentBladeOni.class)
class SilentBladeOniTest extends BaseCardTest {

    @Test
    @CardUsed(GrizzlyBears.class)
    @DisplayName("Ninjutsu returns the unblocked attacker and puts Silent-Blade Oni in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SilentBladeOni()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent silentBladeOni = findPermanent(player1, "Silent-Blade Oni");
        assertThat(silentBladeOni.isTapped()).isTrue();
        assertThat(silentBladeOni.isAttacking()).isTrue();
        assertThat(silentBladeOni.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @CardUsed(GrizzlyBears.class)
    @DisplayName("Combat damage offers a spell from the damaged player's hand for free")
    void castsSpellFromDamagedPlayersHandForFree() {
        addAttackingSilentBladeOni(player1);
        GrizzlyBears controllerCard = new GrizzlyBears();
        GrizzlyBears damagedPlayerCard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(controllerCard)));
        harness.setHand(player2, new ArrayList<>(List.of(damagedPlayerCard)));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getId()).isEqualTo(damagedPlayerCard.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(damagedPlayerCard.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(controllerCard.getId()));

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed(GrizzlyBears.class)
    @DisplayName("Declining leaves the damaged player's spell in hand")
    void decliningLeavesSpellInDamagedPlayersHand() {
        addAttackingSilentBladeOni(player1);
        GrizzlyBears damagedPlayerCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(damagedPlayerCard)));

        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(damagedPlayerCard.getId()));
    }

    @Test
    @CardUsed(Forest.class)
    @DisplayName("Lands in the damaged player's hand are not offered")
    void doesNotOfferLand() {
        addAttackingSilentBladeOni(player1);
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(GrizzlyBears.class)
    @DisplayName("Blocked damage that does not reach a player does not trigger")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        addAttackingSilentBladeOni(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttackingSilentBladeOni(Player player) {
        Permanent silentBladeOni = addCreatureReady(player, new SilentBladeOni());
        silentBladeOni.setAttacking(true);
        return silentBladeOni;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
