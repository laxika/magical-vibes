package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.h.HussarPatrol;
import com.github.laxika.magicalvibes.cards.v.VoltaicKey;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalTitan.class, WoollyThoctar.class, GorillaWarrior.class, Cathodion.class, VoltaicKey.class})
class OpalTitanTest extends BaseCardTest {

    private Permanent addOpalTitan() {
        return harness.addToBattlefieldAndReturn(player1, new OpalTitan());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castOpponentWoollyThoctar() {
        harness.castFromHand(player2, new WoollyThoctar(), "{R}{G}{W}");
    }

    private void castOpponentGorillaWarrior() {
        harness.castFromHand(player2, new GorillaWarrior(), "{2}{G}");
    }

    private void castOpponentHussarPatrol() {
        harness.castFromHand(player2, new HussarPatrol(), "{2}{W}{U}");
    }

    private void castOpponentCathodion() {
        harness.castFromHand(player2, new Cathodion(), "{3}");
    }

    private void castOpponentVoltaicKey() {
        harness.castFromHand(player2, new VoltaicKey(), "{1}");
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Titan a 4/4 Giant with protection from its colors")
    void becomesGiantWithProtectionFromTriggeringSpellColors() {
        Permanent opal = addOpalTitan();
        prepareOpponentCast();

        castOpponentWoollyThoctar();
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

        castOpponentGorillaWarrior();
        resolveAllTriggers();

        castOpponentGorillaWarrior();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Titan")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalTitan();
        prepareOpponentCast();

        castOpponentVoltaicKey();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A controller's creature spell does not trigger Opal Titan")
    void doesNotTriggerForControllerCreatureSpell() {
        Permanent opal = addOpalTitan();

        harness.castFromHand(player1, new GorillaWarrior(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @CardUsed(HussarPatrol.class)
    @DisplayName("The first resolving queued trigger determines Opal Titan's protection")
    void queuedTriggersUseTheirOwnSpellColors() {
        Permanent opal = addOpalTitan();
        prepareOpponentCast();

        castOpponentCathodion();
        castOpponentHussarPatrol();
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.GIANT);
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("A colorless creature spell transforms Opal Titan without granting protection")
    void colorlessCreatureSpellTransformsWithoutProtection() {
        Permanent opal = addOpalTitan();
        prepareOpponentCast();

        castOpponentCathodion();
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.GIANT);
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.GREEN)).isFalse();
    }

    @Test
    @CardUsed(Counterspell.class)
    @DisplayName("A countered creature spell still supplies its colors to Opal Titan")
    void counteredCreatureSpellStillGrantsProtection() {
        Permanent opal = addOpalTitan();
        prepareOpponentCast();

        GorillaWarrior gorillaWarrior = new GorillaWarrior();
        harness.castFromHand(player2, gorillaWarrior, "{2}{G}");
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, gorillaWarrior.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.GREEN)).isTrue();
    }
}
