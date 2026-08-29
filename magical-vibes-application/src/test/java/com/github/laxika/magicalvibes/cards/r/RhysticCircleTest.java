package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RhysticCircleTest extends BaseCardTest {

    @Test
    @DisplayName("If no player pays, the ability prompts for a source choice")
    void noPlayerPaysPromptsForSource() {
        addReadyCircle(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateAndDeclinePayment(player1, player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player1.getId())
                        && shield.sourceId().equals(goblin.getId()));
    }

    @Test
    @DisplayName("A player paying prevents the source-choice effect")
    void paymentPreventsSourceChoice() {
        addReadyCircle(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("The chosen source's next damage to the controller is prevented")
    void chosenSourceDamageIsPrevented() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateAndDeclinePayment(player1, player2);
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private void activateAndDeclinePayment(Player firstPlayer, Player secondPlayer) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(firstPlayer, false);
        harness.handleMayAbilityChosen(secondPlayer, false);
    }

    private Permanent addReadyCircle(Player player) {
        Permanent permanent = new Permanent(new RhysticCircle());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyGoblin(Player player) {
        Permanent permanent = new Permanent(new GoblinPiker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
