package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AutumnWillow.class, AnabaShaman.class, AlibansTower.class})
class AutumnWillowTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target Autumn Willow with a spell while it has shroud")
    void opponentCannotTargetWithSpellWhileShrouded() {
        Permanent willow = addBlockingWillow();

        giveTowerTo(player2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, willow.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("An opponent cannot target Autumn Willow with an ability while it has shroud")
    void opponentCannotTargetWithAbilityWhileShrouded() {
        Permanent willow = addWillow();
        Permanent shaman = addCreatureReady(player2, new AnabaShaman());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        int shamanIndex = gd.playerBattlefields.get(player2.getId()).indexOf(shaman);
        assertThatThrownBy(() -> harness.activateAbility(player2, shamanIndex, null, willow.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("After the ability resolves the targeted player may target Autumn Willow with a spell")
    void targetedPlayerMayTargetWithSpell() {
        Permanent willow = addBlockingWillow();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        giveTowerTo(player2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, willow.getId());
        harness.passBothPriorities();

        assertThat(willow.getPowerModifier()).isEqualTo(3);
        assertThat(willow.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("After the ability resolves the targeted player may target Autumn Willow with an ability")
    void targetedPlayerMayTargetWithAbility() {
        Permanent willow = addWillow();
        Permanent shaman = addCreatureReady(player2, new AnabaShaman());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        int shamanIndex = gd.playerBattlefields.get(player2.getId()).indexOf(shaman);
        harness.activateAbility(player2, shamanIndex, null, willow.getId());
        harness.passBothPriorities();

        assertThat(willow.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the targeted player gains the permission")
    void permissionIsPerPlayer() {
        Permanent willow = addBlockingWillow();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        giveTowerTo(player1);
        harness.castInstant(player1, 0, willow.getId());
        harness.passBothPriorities();
        assertThat(willow.getPowerModifier()).isEqualTo(3);
        assertThat(willow.getToughnessModifier()).isEqualTo(1);

        giveTowerTo(player2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, willow.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("The permission wears off at end of turn")
    void permissionWearsOff() {
        Permanent willow = addWillow();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        willow.setBlocking(true);
        giveTowerTo(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, willow.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    private Permanent addWillow() {
        harness.addToBattlefield(player1, new AutumnWillow());
        return findPermanent(player1, "Autumn Willow");
    }

    private Permanent addBlockingWillow() {
        Permanent willow = addWillow();
        willow.setBlocking(true);
        return willow;
    }

    private void giveTowerTo(Player player) {
        harness.setHand(player, List.of(new AlibansTower()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
