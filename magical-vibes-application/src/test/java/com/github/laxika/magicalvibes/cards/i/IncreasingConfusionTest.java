package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncreasingConfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Has target-player X mill effect and flashback cost")
    void hasCorrectEffects() {
        IncreasingConfusion card = new IncreasingConfusion();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ConditionalReplacementEffect.class);

        ConditionalReplacementEffect effect = (ConditionalReplacementEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        // Cast from graveyard (flashback) mills twice X; otherwise mills X.
        assertThat(effect.condition()).isEqualTo(new CastFromZone(Zone.GRAVEYARD));
        assertThat(((MillTargetPlayerEffect) effect.baseEffect()).count()).isEqualTo(new XValue());
        assertThat(((MillTargetPlayerEffect) effect.upgradedEffect()).count()).isEqualTo(new Scaled(new XValue(), 2));

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{X}{U}");
    }

    @Test
    @DisplayName("Casting normally mills target player for X cards")
    void castingNormallyMillsTargetForX() {
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new IncreasingConfusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Confusion"));
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new IncreasingConfusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2 + 1);
    }

    @Test
    @DisplayName("X=0 mills no cards")
    void xZeroMillsNoCards() {
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new IncreasingConfusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flashback mills target player for twice X")
    void flashbackMillsTargetForTwiceX() {
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setGraveyard(player1, List.of(new IncreasingConfusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Increasing Confusion"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Confusion"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack with X and target")
    void flashbackPutsSpellOnStackWithXAndTarget() {
        harness.setGraveyard(player1, List.of(new IncreasingConfusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, 2, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Increasing Confusion");
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast without a target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new IncreasingConfusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a target");
    }
}
