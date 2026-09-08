package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RhysticLightningTest extends BaseCardTest {

    @Test
    @DisplayName("The target player pays {2} and is dealt 2 damage")
    void targetPlayerPaysToReduceDamage() {
        int lifeBefore = gd.getLife(player2.getId());
        castAtPlayer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The target player declines and is dealt 4 damage")
    void targetPlayerDeclinesToTakeFullDamage() {
        int lifeBefore = gd.getLife(player2.getId());
        castAtPlayer();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("The target creature's controller pays {2} and the creature is dealt 2 damage")
    void targetCreatureControllerPaysToReduceDamage() {
        Permanent target = addCreatureReady(player2, new GiantSpider());
        castAtTarget(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Giant Spider")).isNotNull();
    }

    @Test
    @DisplayName("The target creature's controller declines and the creature is dealt 4 damage")
    void targetCreatureControllerDeclinesToTakeFullDamage() {
        Permanent target = addCreatureReady(player2, new GiantSpider());
        castAtTarget(target);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    private void castAtPlayer() {
        harness.setHand(player1, java.util.List.of(new RhysticLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void castAtTarget(Permanent target) {
        harness.setHand(player1, java.util.List.of(new RhysticLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
