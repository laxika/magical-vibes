package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InkshapeDemonstratorTest extends BaseCardTest {

    @Test
    @DisplayName("Has Ward {2} and a creature-targeting Repartee trigger")
    void hasCorrectStructure() {
        InkshapeDemonstrator card = new InkshapeDemonstrator();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL))
                .singleElement().isInstanceOf(CounterUnlessPaysEffect.class);
        assertThat(((CounterUnlessPaysEffect) card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL).getFirst()).amount())
                .isEqualTo(2);

        SpellCastTriggerEffect trigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.castSpellTargetCondition()).isInstanceOf(StackEntryTargetsPermanentPredicate.class);
        assertThat(trigger.resolvedEffects()).hasSize(2);
    }

    @Test
    @DisplayName("Repartee gives +1/+0 and lifelink until end of turn")
    void reparteeBuffsAndGrantsLifelink() {
        harness.addToBattlefield(player1, new InkshapeDemonstrator());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent demonstrator = findPermanent(player1, "Inkshape Demonstrator");
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isFalse();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, demonstrator)).isEqualTo(4); // 3 base + 1
        assertThat(gqs.getEffectiveToughness(gd, demonstrator)).isEqualTo(4); // unchanged
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Repartee buff and lifelink wear off at cleanup")
    void reparteeWearsOff() {
        harness.addToBattlefield(player1, new InkshapeDemonstrator());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent demonstrator = findPermanent(player1, "Inkshape Demonstrator");
        assertThat(gqs.getEffectivePower(gd, demonstrator)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent demonstrator = new Permanent(new InkshapeDemonstrator());
        demonstrator.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(demonstrator);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1); // exact cost, cannot pay Ward

        harness.castInstant(player2, 0, demonstrator.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Shock"));
    }
}
