package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressiveFiredancerTest extends BaseCardTest {

    private Permanent addFiredancer(Player player) {
        ExpressiveFiredancer card = new ExpressiveFiredancer();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Has instant/sorcery spell-cast trigger with +1/+1 and conditional double strike grant")
    void hasCorrectEffects() {
        ExpressiveFiredancer card = new ExpressiveFiredancer();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        SpellCastTriggerEffect trigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();

        assertThat(trigger.resolvedEffects()).hasSize(2);
        assertThat(trigger.resolvedEffects().get(0)).isInstanceOf(BoostSelfEffect.class);
        assertThat(trigger.resolvedEffects().get(1)).isInstanceOf(ConditionalEffect.class);

        BoostSelfEffect boost = (BoostSelfEffect) trigger.resolvedEffects().get(0);
        assertThat(boost.powerBoost()).isEqualTo(new Fixed(1));
        assertThat(boost.toughnessBoost()).isEqualTo(new Fixed(1));

        ConditionalEffect conditional = (ConditionalEffect) trigger.resolvedEffects().get(1);
        assertThat(((SpellManaSpentAtLeast) conditional.condition()).minMana()).isEqualTo(5);
        assertThat(conditional.wrapped()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) conditional.wrapped();
        assertThat(grant.scope()).isEqualTo(GrantScope.SELF);
        assertThat(grant.keywords()).containsExactly(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("Casting a one-mana instant gives +1/+1 and no double strike")
    void cheapSpellBoostsWithoutDoubleStrike() {
        Permanent firedancer = addFiredancer(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(firedancer.getPowerModifier()).isEqualTo(1);
        assertThat(firedancer.getToughnessModifier()).isEqualTo(1);
        assertThat(firedancer.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("Casting a four-mana spell gives +1/+1 but no double strike")
    void fourManaSpellBoostsWithoutDoubleStrike() {
        Permanent firedancer = addFiredancer(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(firedancer.getPowerModifier()).isEqualTo(1);
        assertThat(firedancer.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("Casting a five-mana spell gives +1/+1 and double strike")
    void fiveManaSpellGrantsDoubleStrike() {
        Permanent firedancer = addFiredancer(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(firedancer.getPowerModifier()).isEqualTo(1);
        assertThat(firedancer.getToughnessModifier()).isEqualTo(1);
        assertThat(firedancer.getGrantedKeywords()).contains(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent firedancer = addFiredancer(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(firedancer.getPowerModifier()).isZero();
        assertThat(firedancer.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
    }
}
