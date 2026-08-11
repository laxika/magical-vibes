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

class OpalArchangelTest extends BaseCardTest {

    private Permanent addOpalArchangel() {
        return harness.addToBattlefieldAndReturn(player1, new OpalArchangel());
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

    private void resolveOpponentCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Archangel a 5/5 Angel creature with flying and vigilance")
    void becomesAngelCreatureWhenOpponentCastsCreature() {
        Permanent opal = addOpalArchangel();
        prepareOpponentCast();

        castOpponentCreature();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opal, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The trigger does not fire after Opal Archangel has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent opal = addOpalArchangel();
        prepareOpponentCast();

        castOpponentCreature();
        resolveOpponentCreatureAndTrigger();
        castOpponentCreature();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Archangel")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalArchangel();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }
}
