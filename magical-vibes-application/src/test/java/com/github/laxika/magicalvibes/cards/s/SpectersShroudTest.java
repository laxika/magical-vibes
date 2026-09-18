package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectersShroud.class, DarksteelGargoyle.class})
class SpectersShroudTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsPowerBoost() {
        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent shroud = addShroudReady(player1);
        shroud.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {1} attaches Specter's Shroud to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent shroud = addShroudReady(player1);
        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shroud.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by another player")
    void equipCannotTargetOpponentsCreature() {
        Permanent shroud = addShroudReady(player1);
        Permanent creature = addCreatureReady(player2, new DarksteelGargoyle());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shroud.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage makes the damaged player discard")
    void combatDamageMakesDamagedPlayerDiscard() {
        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent shroud = addShroudReady(player1);
        shroud.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setHand(player2, List.of(new DarksteelGargoyle(), new DarksteelGargoyle()));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No discard trigger occurs when the equipped creature deals no combat damage to a player")
    void noDiscardWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent shroud = addShroudReady(player1);
        shroud.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new DarksteelGargoyle());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, List.of(new DarksteelGargoyle()));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("An unattached Specter's Shroud does not trigger on combat damage")
    void unattachedShroudDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        addShroudReady(player1);
        creature.setAttacking(true);
        harness.setHand(player2, List.of(new DarksteelGargoyle()));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private Permanent addShroudReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SpectersShroud());
    }
}
