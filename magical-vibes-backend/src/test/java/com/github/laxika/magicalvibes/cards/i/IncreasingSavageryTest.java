package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CastFromHandConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncreasingSavageryTest extends BaseCardTest {

    @Test
    @DisplayName("Has target creature counter effects and flashback cost")
    void hasCorrectEffects() {
        IncreasingSavagery card = new IncreasingSavagery();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
        PutPlusOnePlusOneCounterOnTargetCreatureEffect baseEffect =
                (PutPlusOnePlusOneCounterOnTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(baseEffect.count()).isEqualTo(5);

        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(CastFromHandConditionalEffect.class);
        CastFromHandConditionalEffect conditional =
                (CastFromHandConditionalEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(conditional.sourceZone()).isEqualTo(Zone.GRAVEYARD);
        assertThat(conditional.wrapped()).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
        PutPlusOnePlusOneCounterOnTargetCreatureEffect flashbackExtra =
                (PutPlusOnePlusOneCounterOnTargetCreatureEffect) conditional.wrapped();
        assertThat(flashbackExtra.count()).isEqualTo(5);

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{5}{G}{G}");
    }

    @Test
    @DisplayName("Normal cast puts five +1/+1 counters on target creature")
    void normalCastPutsFiveCountersOnTargetCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncreasingSavagery()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Savagery"));
    }

    @Test
    @DisplayName("Flashback puts ten +1/+1 counters on target creature and exiles the spell")
    void flashbackPutsTenCountersAndExilesSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new IncreasingSavagery()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(10);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Increasing Savagery"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Savagery"));
    }

    @Test
    @DisplayName("Flashback puts the sorcery on stack as cast from graveyard")
    void flashbackPutsSorceryOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new IncreasingSavagery()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, bearId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Increasing Savagery");
        assertThat(entry.isCastWithFlashback()).isTrue();
        assertThat(entry.getSourceZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DarksteelAxe());
        harness.setHand(player1, List.of(new IncreasingSavagery()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID axeId = harness.getPermanentId(player1, "Darksteel Axe");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, axeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
