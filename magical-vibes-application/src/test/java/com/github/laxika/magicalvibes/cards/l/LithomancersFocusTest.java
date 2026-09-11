package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LithomancersFocus.class, GrizzlyBears.class})
class LithomancersFocusTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a creature and prevents damage to it from colorless sources")
    void boostsAndProtectsFromColorlessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFocus(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        castDamage(player2, colorlessDamageSpell(), target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from colored sources")
    void doesNotPreventColoredDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFocus(target);

        castDamage(player2, coloredDamageSpell(CardColor.RED), target);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The boost and damage prevention expire at the end of the turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFocus(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        castDamage(player2, colorlessDamageSpell(), target);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new LithomancersFocus()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFocus(Permanent target) {
        harness.setHand(player1, List.of(new LithomancersFocus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void castDamage(com.github.laxika.magicalvibes.model.Player caster, Card damageSpell,
                             Permanent target) {
        harness.setHand(caster, List.of(damageSpell));
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private Card colorlessDamageSpell() {
        return damageSpell("Colorless Bolt", null);
    }

    private Card coloredDamageSpell(CardColor color) {
        return damageSpell("Red Bolt", color);
    }

    private Card damageSpell(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));
        return card;
    }
}
