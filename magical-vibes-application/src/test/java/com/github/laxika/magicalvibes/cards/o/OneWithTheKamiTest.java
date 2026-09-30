package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OneWithTheKami.class, GrizzlyBears.class, Murder.class})
class OneWithTheKamiTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature creates Spirits equal to its power when it dies")
    void enchantedCreatureDeathCreatesSpiritsEqualToPower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAuraOn(bears);

        killWithMurder(player2, bears.getId());

        assertSpiritTokens(2);
    }

    @Test
    @DisplayName("Another modified creature you control creates Spirits equal to its power when it dies")
    void anotherModifiedCreatureDeathCreatesSpiritsEqualToPower() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent modified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castAuraOn(enchanted);

        killWithMurder(player2, modified.getId());

        assertSpiritTokens(3);
    }

    @Test
    @DisplayName("Unmodified and opponent creatures do not trigger One with the Kami")
    void unrelatedCreatureDeathsDoNotTrigger() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unmodified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentModified = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentModified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castAuraOn(enchanted);

        killWithMurder(player2, unmodified.getId());
        killWithMurder(player1, opponentModified.getId());

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One with the Kami can enchant only a creature you control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OneWithTheKami()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castAuraOn(Permanent target) {
        harness.setHand(player1, List.of(new OneWithTheKami()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killWithMurder(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void assertSpiritTokens(int expectedCount) {
        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(expectedCount);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.getCard().getColor()).isNull();
            assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(spirit.getCard().getPower()).isEqualTo(1);
            assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        });
    }
}
