package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BraceForImpact.class, WoollyThoctar.class, Shock.class, LightningBolt.class, GrizzlyBears.class})
class BraceForImpactTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage to a multicolored creature and adds a counter for each damage prevented")
    void preventsAllDamageAndAddsCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());

        castBraceForImpact(target);
        castDamage(player2, new Shock(), target, ManaColor.RED);
        castDamage(player2, new LightningBolt(), target, ManaColor.RED);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Prevention and its counter rider expire at the end of the turn")
    void expiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());

        castBraceForImpact(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castDamage(player2, new Shock(), target, ManaColor.RED);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a monocolored creature")
    void cannotTargetMonocoloredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BraceForImpact()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBraceForImpact(Permanent target) {
        harness.setHand(player1, List.of(new BraceForImpact()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void castDamage(Player caster, Card damageCard, Permanent target, ManaColor manaColor) {
        harness.setHand(caster, List.of(damageCard));
        harness.addMana(caster, manaColor, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
