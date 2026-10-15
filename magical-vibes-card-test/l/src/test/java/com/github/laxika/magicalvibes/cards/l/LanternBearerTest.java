package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LanternBearer.class, GrizzlyBears.class})
class LanternBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts from graveyard transformed as Lanterns' Lift attached to a creature")
    void disturbEntersTransformedAttached() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LanternBearer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, bears.getId());

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and flying")
    void enchantedCreatureGetsBoostAndFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        putLiftAttachedTo(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying end when the Aura is removed")
    void boostAndFlyingStopWhenAuraRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = putLiftAttachedTo(bears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Lanterns' Lift is exiled instead of going to the graveyard")
    void liftExiledInsteadOfGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = putLiftAttachedTo(bears);
        UUID auraCardId = aura.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

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

    @Test
    @DisplayName("Lantern Bearer goes to the graveyard normally with its front face up")
    void frontFaceIsNotExiledWhenItDies() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new LanternBearer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bearer));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bearer.getOriginalCard());
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Disturbed Aura is exiled if its target leaves before resolution")
    void illegalTargetExilesDisturbedAura() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        LanternBearer bearer = new LanternBearer();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(bearer));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0, bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears.getOriginalCard());
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(bearer.getId());
    }

    @Test
    @DisplayName("Lanterns' Lift can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent aura = putLiftAttachedTo(bears);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Lanterns' Lift is exiled when its enchanted creature dies")
    void unattachedAuraIsExiled() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = putLiftAttachedTo(bears);
        UUID auraCardId = aura.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears.getOriginalCard());
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(auraCardId);
    }

    @Test
    @DisplayName("Disturb cannot be paid with only the front face's mana cost")
    void disturbRequiresFullCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        LanternBearer bearer = new LanternBearer();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(bearer));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bearer);
    }

    private Permanent putLiftAttachedTo(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LanternBearer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveFlashback(player1, 0, creature.getId());
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
    }
}
