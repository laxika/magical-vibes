package com.github.laxika.magicalvibes.cards.t;

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

class TwinbladeGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts from graveyard transformed as Twinblade Invocation attached to a creature")
    void disturbEntersTransformedAttached() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TwinbladeGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
        assertThat(aura.getCard().getName()).isEqualTo("Twinblade Invocation");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature has double strike")
    void enchantedCreatureHasDoubleStrike() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        putInvocationAttachedTo(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses double strike when the Aura is removed")
    void doubleStrikeStopsWhenAuraRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent aura = putInvocationAttachedTo(bears);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Twinblade Invocation is exiled instead of going to the graveyard")
    void invocationExiledInsteadOfGraveyard() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent aura = putInvocationAttachedTo(bears);
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
        harness.setGraveyard(player1, List.of(new TwinbladeGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    private Permanent putInvocationAttachedTo(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TwinbladeGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
    }
}
