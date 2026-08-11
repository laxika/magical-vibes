package com.github.laxika.magicalvibes.cards.o;

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

class OpalGargoyleTest extends BaseCardTest {

    private Permanent addOpalGargoyle() {
        return harness.addToBattlefieldAndReturn(player1, new OpalGargoyle());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castOpponentCreature() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Gargoyle a 2/2 Gargoyle creature with flying")
    void becomesGargoyleCreatureWhenOpponentCastsCreature() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();

        castOpponentCreature();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.GARGOYLE);
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The trigger does not fire after Opal Gargoyle has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();

        castOpponentCreature();
        harness.passBothPriorities();
        harness.passBothPriorities();
        castOpponentCreature();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Gargoyle")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }
}
