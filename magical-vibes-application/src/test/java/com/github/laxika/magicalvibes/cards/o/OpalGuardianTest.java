package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalGuardian.class, GrizzlyBears.class, Spellbook.class})
class OpalGuardianTest extends BaseCardTest {

    private Permanent addOpalGuardian() {
        return harness.addToBattlefieldAndReturn(player1, new OpalGuardian());
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
    @DisplayName("An opponent's creature spell makes Opal Guardian a 3/4 Gargoyle with flying and protection from red")
    void becomesGargoyleWithFlyingAndProtectionFromRed() {
        Permanent opal = addOpalGuardian();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.RED)).isFalse();
        prepareOpponentCast();

        castOpponentCreature();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.GARGOYLE);
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("The trigger does not fire after Opal Guardian has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent opal = addOpalGuardian();
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
    @DisplayName("A noncreature spell does not trigger Opal Guardian")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalGuardian();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }
}
