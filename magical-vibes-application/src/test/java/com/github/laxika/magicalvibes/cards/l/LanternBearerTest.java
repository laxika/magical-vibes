package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LanternBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts from graveyard transformed as Lanterns' Lift attached to a creature")
    void disturbEntersTransformedAttached() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LanternBearer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
        assertThat(aura.getCard().getName()).isEqualTo("Lanterns' Lift");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and flying")
    void enchantedCreatureGetsBoostAndFlying() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        putLiftAttachedTo(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying end when the Aura is removed")
    void boostAndFlyingStopWhenAuraRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent aura = putLiftAttachedTo(bears);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Lanterns' Lift is exiled instead of going to the graveyard")
    void liftExiledInsteadOfGraveyard() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent aura = putLiftAttachedTo(bears);
        UUID auraCardId = aura.getOriginalCard().getId();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(auraCardId);
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LanternBearer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    private Permanent putLiftAttachedTo(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LanternBearer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
    }
}
