package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ColorstormStallionTest extends BaseCardTest {

    private Permanent addStallion(Player player) {
        ColorstormStallion card = new ColorstormStallion();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private long countStallionTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Colorstorm Stallion") && p.getCard().isToken())
                .count();
    }

    @Test
    @DisplayName("Has instant/sorcery spell-cast trigger with +1/+1 boost and conditional token copy")
    void hasCorrectEffects() {
        ColorstormStallion card = new ColorstormStallion();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.resolvedEffects()).hasSize(2);
        assertThat(trigger.resolvedEffects().get(0)).isInstanceOf(BoostSelfEffect.class);
        assertThat(trigger.resolvedEffects().get(1)).isInstanceOf(ConditionalEffect.class);

        BoostSelfEffect boost = (BoostSelfEffect) trigger.resolvedEffects().get(0);
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);

        ConditionalEffect conditional =
                (ConditionalEffect) trigger.resolvedEffects().get(1);
        assertThat(((SpellManaSpentAtLeast) conditional.condition()).minMana()).isEqualTo(5);
        assertThat(conditional.wrapped()).isInstanceOf(CreateTokenCopyOfSourceEffect.class);
    }

    @Test
    @DisplayName("Casting a one-mana instant gives +1/+1 and no token")
    void castingCheapInstantBoostsWithoutToken() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(1);
        assertThat(stallion.getToughnessModifier()).isEqualTo(1);
        assertThat(countStallionTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Casting a spell with four mana spent gives +1/+1 but no token")
    void castingFourManaSpellBoostsWithoutToken() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(1);
        assertThat(stallion.getToughnessModifier()).isEqualTo(1);
        assertThat(countStallionTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Casting a spell with five or more mana spent gives +1/+1 and a token copy")
    void castingFiveManaSpellCreatesTokenCopy() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(1);
        assertThat(stallion.getToughnessModifier()).isEqualTo(1);
        assertThat(countStallionTokens(player1)).isEqualTo(1);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Colorstorm Stallion") && p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
    }

    @Test
    @DisplayName("Multiple instant casts stack the boost and create tokens when mana threshold is met")
    void multipleCastsStackBoostAndTokens() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(stallion.getPowerModifier()).isEqualTo(1);
        assertThat(countStallionTokens(player1)).isZero();

        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(2);
        assertThat(stallion.getToughnessModifier()).isEqualTo(2);
        assertThat(countStallionTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Colorstorm Stallion")
    void castingCreatureDoesNotTrigger() {
        Permanent stallion = addStallion(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(stallion.getPowerModifier()).isEqualTo(0);
        assertThat(stallion.getToughnessModifier()).isEqualTo(0);
        assertThat(countStallionTokens(player1)).isZero();
    }
}
