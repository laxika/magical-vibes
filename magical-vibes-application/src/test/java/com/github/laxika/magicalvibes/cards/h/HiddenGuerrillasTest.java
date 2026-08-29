package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HiddenGuerrillasTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's artifact spell makes Hidden Guerrillas a 5/3 Soldier creature with trample")
    void becomesSoldierCreatureWhenOpponentCastsArtifact() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        prepareOpponentCast();
        harness.setHand(player2, List.of(createArtifactSpell()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenGuerrillas)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenGuerrillas)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hiddenGuerrillas)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hiddenGuerrillas)).containsExactly(CardSubtype.SOLDIER);
        assertThat(gqs.hasKeyword(gd, hiddenGuerrillas, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A non-artifact spell does not trigger Hidden Guerrillas")
    void doesNotTriggerForNonArtifactSpell() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        prepareOpponentCast();
        harness.setHand(player2, List.of(createEnchantmentSpell()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenGuerrillas)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger when its controller casts an artifact spell")
    void doesNotTriggerForControllerArtifactSpell() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        harness.setHand(player1, List.of(createArtifactSpell()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenGuerrillas)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isFalse();
    }

    @Test
    @DisplayName("Hidden Guerrillas does not trigger again after becoming a creature")
    void doesNotTriggerAfterBecomingCreature() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        prepareOpponentCast();
        harness.setHand(player2, List.of(createArtifactSpell()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isTrue();

        harness.setHand(player2, List.of(createArtifactSpell()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isTrue();
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Card createArtifactSpell() {
        Card card = new Card();
        card.setName("Test Artifact");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{1}");
        return card;
    }

    private Card createEnchantmentSpell() {
        Card card = new Card();
        card.setName("Test Enchantment");
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        return card;
    }
}
