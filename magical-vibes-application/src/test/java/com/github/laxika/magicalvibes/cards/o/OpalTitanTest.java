package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpalTitanTest extends BaseCardTest {

    private Permanent addOpalTitan() {
        return harness.addToBattlefieldAndReturn(player1, new OpalTitan());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Titan a 4/4 Giant with protection from its colors")
    void becomesGiantWithProtectionFromTriggeringSpellColors() {
        Permanent opal = addOpalTitan();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new WoollyThoctar()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.GIANT);
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("The trigger does not fire after Opal Titan has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent opal = addOpalTitan();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Titan")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalTitan();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }
}
