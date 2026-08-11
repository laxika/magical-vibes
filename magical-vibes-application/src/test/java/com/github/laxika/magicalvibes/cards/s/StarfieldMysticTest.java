package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StarfieldMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Enchantment spells you cast cost {1} less")
    void enchantmentSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StarfieldMystic());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Non-enchantment spells are not reduced")
    void nonEnchantmentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StarfieldMystic());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The cost reduction only applies to the controller's enchantment spells")
    void opponentEnchantmentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StarfieldMystic());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A controlled enchantment dying puts a +1/+1 counter on Starfield Mystic")
    void controlledEnchantmentDyingAddsCounter() {
        harness.addToBattlefield(player1, new StarfieldMystic());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent mystic = findPermanent(player1, "Starfield Mystic");
        UUID anthemId = harness.getPermanentId(player1, "Glorious Anthem");

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, anthemId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(mystic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(mystic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's enchantment dying does not trigger Starfield Mystic")
    void opponentEnchantmentDyingDoesNotAddCounter() {
        harness.addToBattlefield(player1, new StarfieldMystic());
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent mystic = findPermanent(player1, "Starfield Mystic");
        UUID anthemId = harness.getPermanentId(player2, "Glorious Anthem");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, anthemId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(mystic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
