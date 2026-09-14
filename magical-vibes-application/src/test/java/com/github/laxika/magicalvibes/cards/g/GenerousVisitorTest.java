package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenerousVisitor.class, IntangibleVirtue.class, GrizzlyBears.class, Spellbook.class})
class GenerousVisitorTest extends BaseCardTest {

    @Test
    void enchantmentSpellPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new GenerousVisitor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntangibleVirtue()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonEnchantmentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GenerousVisitor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GenerousVisitor());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new IntangibleVirtue()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
