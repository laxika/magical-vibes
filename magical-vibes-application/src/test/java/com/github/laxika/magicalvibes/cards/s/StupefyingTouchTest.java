package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.c.CephalidIllusionist;
import com.github.laxika.magicalvibes.cards.t.TaintedIsle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StupefyingTouch.class, AngelOfRetribution.class, CephalidIllusionist.class, TaintedIsle.class})
class StupefyingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Stupefying Touch enters attached to a creature and draws a card")
    void entersAttachedAndDrawsCard() {
        Permanent creature = addCreatureReady(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(new StupefyingTouch()));
        harness.setLibrary(player1, List.of(new AngelOfRetribution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Stupefying Touch").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInHand(player1, "Angel of Retribution");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent creature = addCreatureReady(player1, new CephalidIllusionist());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new StupefyingTouch());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Stupefying Touch cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new TaintedIsle());
        harness.setHand(player1, List.of(new StupefyingTouch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
