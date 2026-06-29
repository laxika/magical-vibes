package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemorialToGeniusTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Memorial to Genius has correct card structure")
    void hasCorrectProperties() {
        MemorialToGenius card = new MemorialToGenius();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasAtLeastOneElementOfType(EntersTappedEffect.class);
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var sacrificeAbility = card.getActivatedAbilities().get(0);
        assertThat(sacrificeAbility.isRequiresTap()).isTrue();
        assertThat(sacrificeAbility.getManaCost()).isEqualTo("{4}{U}");
        assertThat(sacrificeAbility.getEffects()).hasSize(2);
        assertThat(sacrificeAbility.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(sacrificeAbility.getEffects().get(1)).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Memorial to Genius enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new MemorialToGenius()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent memorial = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Memorial to Genius"))
                .findFirst().orElseThrow();
        assertThat(memorial.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Memorial to Genius produces blue mana")
    void tappingProducesBlueMana() {
        Permanent memorial = addMemorialReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(memorial);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    // ===== Sacrifice ability =====

    @Test
    @DisplayName("Activating sacrifice ability puts it on the stack")
    void sacrificeAbilityPutsOnStack() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Memorial to Genius");
    }

    @Test
    @DisplayName("Memorial is sacrificed as a cost before resolution")
    void sacrificedBeforeResolution() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Memorial to Genius"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memorial to Genius"));
    }

    @Test
    @DisplayName("Resolving sacrifice ability draws two cards")
    void resolvingSacrificeAbilityDrawsTwoCards() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Mana is consumed when activating sacrifice ability")
    void manaIsConsumedWhenActivating() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent memorial = addMemorialReady(player1);
        memorial.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Helper methods =====

    private Permanent addMemorialReady(Player player) {
        MemorialToGenius card = new MemorialToGenius();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
