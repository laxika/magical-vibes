package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpalCaryatidTest extends BaseCardTest {

    private Permanent addOpalCaryatid() {
        return harness.addToBattlefieldAndReturn(player1, new OpalCaryatid());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Caryatid a 2/2 Soldier creature")
    void becomesSoldierCreatureWhenOpponentCastsCreature() {
        Permanent opal = addOpalCaryatid();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Caryatid")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalCaryatid();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("Opal Caryatid does not trigger for its controller's creature spell")
    void doesNotTriggerForControllerCreatureSpell() {
        Permanent opal = addOpalCaryatid();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }
}
