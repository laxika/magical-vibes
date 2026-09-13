package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LesserGargadon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhysticLightning.class, LesserGargadon.class})
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
        Permanent target = addCreatureReady(player2, new LesserGargadon());
        castAtTarget(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Lesser Gargadon")).isNotNull();
    }

    @Test
    @DisplayName("The target creature's controller declines and the creature is dealt 4 damage")
    void targetCreatureControllerDeclinesToTakeFullDamage() {
        Permanent target = addCreatureReady(player2, new LesserGargadon());
        castAtTarget(target);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Lesser Gargadon");
        harness.assertInGraveyard(player2, "Lesser Gargadon");
    }

    private void castAtPlayer() {
        harness.setHand(player1, java.util.List.of(new RhysticLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    private void castAtTarget(Permanent target) {
        harness.setHand(player1, java.util.List.of(new RhysticLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
