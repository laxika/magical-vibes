package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VeilOfBirdsTest extends BaseCardTest {

    private Permanent addVeilOfBirds() {
        return harness.addToBattlefieldAndReturn(player1, new VeilOfBirds());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Becomes a 1/1 Bird creature with flying when an opponent casts a spell")
    void becomesBirdCreatureWhenOpponentCastsSpell() {
        Permanent veil = addVeilOfBirds();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, veil)).isTrue();
        assertThat(gqs.isEnchantment(gd, veil)).isFalse();
        assertThat(gqs.getEffectivePower(gd, veil)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, veil)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, veil)).containsExactly(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, veil, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when its controller casts a spell")
    void doesNotTriggerForControllerCast() {
        Permanent veil = addVeilOfBirds();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, veil)).isTrue();
        assertThat(gqs.isCreature(gd, veil)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger again after becoming a creature")
    void doesNotTriggerAfterBecomingCreature() {
        Permanent veil = addVeilOfBirds();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        prepareOpponentCast();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, veil)).isTrue();
        assertThat(gqs.isEnchantment(gd, veil)).isFalse();
    }
}
