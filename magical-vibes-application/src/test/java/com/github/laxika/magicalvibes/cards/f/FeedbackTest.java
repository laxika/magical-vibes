package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Feedback.class, BadMoon.class, CircleOfProtectionBlue.class, GrizzlyBears.class})
class FeedbackTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant an enchantment with Feedback")
    void canEnchantEnchantment() {
        Permanent enchantment = addEnchantment(player2);

        harness.setHand(player1, List.of(new Feedback()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, enchantment.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a non-enchantment creature")
    void cannotEnchantCreature() {
        addEnchantment(player2); // a legal target exists so the Aura is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Feedback()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Resolving Feedback attaches it to the target enchantment")
    void resolvingAttachesToEnchantment() {
        Permanent enchantment = addEnchantment(player2);
        Feedback feedback = new Feedback();

        harness.setHand(player1, List.of(feedback));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        Permanent feedbackPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(feedback.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(feedbackPermanent.isAttached()).isTrue();
        assertThat(feedbackPermanent.getAttachedTo()).isEqualTo(enchantment.getId());
    }

    @Test
    @DisplayName("Enchanted enchantment's controller takes 1 damage at their upkeep")
    void enchantedControllerTakesDamageAtUpkeep() {
        Permanent enchantment = addEnchantment(player2);
        attachFeedback(enchantment);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Feedback does NOT damage the aura controller during their own upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent enchantment = addEnchantment(player2);
        attachFeedback(enchantment);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Damage accumulates over multiple upkeeps")
    void damageAccumulatesOverUpkeeps() {
        Permanent enchantment = addEnchantment(player2);
        attachFeedback(enchantment);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void damageCanBePrevented() {
        Permanent enchantment = addEnchantment(player2);
        harness.addToBattlefield(player2, new CircleOfProtectionBlue());
        Permanent feedback = attachFeedback(enchantment);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, feedback.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    private Permanent attachFeedback(Permanent enchantment) {
        Permanent feedback = harness.addToBattlefieldAndReturn(player1, new Feedback());
        feedback.setAttachedTo(enchantment.getId());
        return feedback;
    }

    private Permanent addEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BadMoon());
    }
}
