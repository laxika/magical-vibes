package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnimateDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Animate Dead targeting a creature card in a graveyard puts it on the stack")
    void castingPutsOnStack() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new AnimateDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Animate Dead");
        assertThat(gd.stack.getFirst().getTargetZone()).isEqualTo(Zone.GRAVEYARD);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving reanimates the creature under your control with the Aura attached")
    void reanimatesAndAttaches() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new AnimateDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Animate Dead")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets -1/-0")
    void enchantedCreatureGetsDebuff() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new AnimateDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reanimates a creature from an opponent's graveyard under your control")
    void reanimatesFromOpponentGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        UUID targetId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new AnimateDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("When the Aura leaves the battlefield, the reanimated creature is sacrificed")
    void sacrificesCreatureWhenAuraLeaves() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new AnimateDead(), new Disenchant()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Animate Dead");
        assertThat(aura).isNotNull();

        harness.addMana(player1, ManaColor.WHITE, 2);
        // Disenchant is now at hand index 0 (Animate Dead was cast out of hand).
        harness.castInstant(player1, 0, aura.getId());
        for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        harness.setGraveyard(player1, List.of(new Disenchant()));
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new AnimateDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
