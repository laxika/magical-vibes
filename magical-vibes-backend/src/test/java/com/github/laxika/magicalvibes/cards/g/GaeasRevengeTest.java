package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetedByNonColorSourcesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GaeasRevengeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Gaea's Revenge has cant-be-countered and non-color targeting restriction")
    void hasCorrectProperties() {
        GaeasRevenge card = new GaeasRevenge();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasAtLeastOneElementOfType(CantBeCounteredEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasAtLeastOneElementOfType(CantBeTargetedByNonColorSourcesEffect.class);
        CantBeTargetedByNonColorSourcesEffect effect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof CantBeTargetedByNonColorSourcesEffect)
                .map(e -> (CantBeTargetedByNonColorSourcesEffect) e)
                .findFirst().orElseThrow();
        assertThat(effect.allowedColor()).isEqualTo(CardColor.GREEN);
    }

    // ===== Can't be countered =====

    @Test
    @DisplayName("Gaea's Revenge cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        GaeasRevenge gaeas = new GaeasRevenge();
        harness.setHand(player1, List.of(gaeas));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, gaeas.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gaea's Revenge"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Gaea's Revenge"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }

    // ===== Can't be targeted by nongreen spells =====

    @Test
    @DisplayName("Opponent cannot target Gaea's Revenge with a red spell")
    void opponentCannotTargetWithNongreenSpell() {
        Permanent gaeasPerm = addGaeasRevengeReady(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, gaeasPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller also cannot target Gaea's Revenge with a nongreen spell")
    void controllerCannotTargetWithNongreenSpell() {
        Permanent gaeasPerm = addGaeasRevengeReady(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, gaeasPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Green spell can target Gaea's Revenge")
    void greenSpellCanTarget() {
        Permanent gaeasPerm = addGaeasRevengeReady(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, gaeasPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Opponent's green spell can also target Gaea's Revenge")
    void opponentGreenSpellCanTarget() {
        Permanent gaeasPerm = addGaeasRevengeReady(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, gaeasPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    // ===== Can't be targeted by abilities from nongreen sources =====

    @Test
    @DisplayName("Abilities from nongreen sources cannot target Gaea's Revenge")
    void abilitiesFromNongreenSourceCannotTarget() {
        Permanent gaeasPerm = addGaeasRevengeReady(player2);

        // ProdigalPyromancer is red, its ability should not be able to target Gaea's Revenge
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gaeasPerm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helper methods =====

    private Permanent addGaeasRevengeReady(Player player) {
        GaeasRevenge card = new GaeasRevenge();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
