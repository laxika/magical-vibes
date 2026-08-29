package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HiddenAncientsTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's enchantment spell makes Hidden Ancients a 5/5 Treefolk creature")
    void becomesTreefolkCreatureWhenOpponentCastsEnchantment() {
        Permanent hiddenAncients = harness.addToBattlefieldAndReturn(player1, new HiddenAncients());
        prepareOpponentCast();
        harness.setHand(player2, List.of(createEnchantmentSpell()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenAncients)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenAncients)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenAncients)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hiddenAncients)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hiddenAncients)).containsExactly(CardSubtype.TREEFOLK);
    }

    @Test
    @DisplayName("A non-enchantment spell does not trigger Hidden Ancients")
    void doesNotTriggerForNonEnchantmentSpell() {
        Permanent hiddenAncients = harness.addToBattlefieldAndReturn(player1, new HiddenAncients());
        prepareOpponentCast();
        harness.setHand(player2, List.of(createArtifactSpell()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenAncients)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenAncients)).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger when its controller casts an enchantment spell")
    void doesNotTriggerForControllerEnchantmentSpell() {
        Permanent hiddenAncients = harness.addToBattlefieldAndReturn(player1, new HiddenAncients());
        harness.setHand(player1, List.of(createEnchantmentSpell()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenAncients)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenAncients)).isFalse();
    }

    @Test
    @DisplayName("Hidden Ancients does not trigger again after becoming a creature")
    void doesNotTriggerAfterBecomingCreature() {
        Permanent hiddenAncients = harness.addToBattlefieldAndReturn(player1, new HiddenAncients());
        prepareOpponentCast();
        harness.setHand(player2, List.of(createEnchantmentSpell()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hiddenAncients)).isTrue();

        harness.setHand(player2, List.of(createEnchantmentSpell()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castEnchantment(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, hiddenAncients)).isTrue();
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Card createEnchantmentSpell() {
        Card card = new Card();
        card.setName("Test Enchantment");
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        return card;
    }

    private Card createArtifactSpell() {
        Card card = new Card();
        card.setName("Test Artifact");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{1}");
        return card;
    }
}
