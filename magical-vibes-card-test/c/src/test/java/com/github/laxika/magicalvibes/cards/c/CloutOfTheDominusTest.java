package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.StreamHopper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloutOfTheDominus.class, FugitiveWizard.class, HillGiant.class,
        GrizzlyBears.class, FountainOfYouth.class, StreamHopper.class})
class CloutOfTheDominusTest extends BaseCardTest {

    @Test
    void blueRedCreatureGetsBothBonusesAfterAuraResolves() {
        Permanent hopper = harness.addToBattlefieldAndReturn(player2, new StreamHopper());
        harness.setHand(player1, List.of(new CloutOfTheDominus()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, hopper.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Clout of the Dominus").getAttachedTo()).isEqualTo(hopper.getId());
        assertThat(gqs.getEffectivePower(gd, hopper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hopper)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hopper, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, hopper, Keyword.HASTE)).isTrue();
    }

    @Test
    void shroudPreventsControllerFromTargetingEnchantedCreature() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        attach(wizard);
        harness.setHand(player1, List.of(new CloutOfTheDominus()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void shroudPreventsOpponentFromTargetingEnchantedCreature() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        attach(wizard);
        harness.setHand(player1, List.of(new CloutOfTheDominus()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void auraResolvesOnBlueCreatureBeforeGrantingShroud() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new CloutOfTheDominus()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, wizard.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Clout of the Dominus").getAttachedTo()).isEqualTo(wizard.getId());
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isTrue();
        harness.assertNotInGraveyard(player1, "Clout of the Dominus");
    }

    private Permanent attach(Permanent creature) {
        Permanent clout = harness.addToBattlefieldAndReturn(player1, new CloutOfTheDominus());
        clout.setAttachedTo(creature.getId());
        return clout;
    }

    @Test
    @DisplayName("Blue enchanted creature gets +1/+1 and shroud")
    void blueGetsBoostAndShroud() {
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        attach(wizard);

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Red enchanted creature gets +1/+1 and haste")
    void redGetsBoostAndHaste() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        attach(giant);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Haste from Clout lets a summoning-sick red creature attack")
    void hasteAllowsSummoningSickAttack() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.setSummoningSick(true);
        attach(giant);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Giant is battlefield index 0 (Clout aura added after). Succeeding proves haste.
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Non-blue non-red enchanted creature gets no boost or keywords")
    void offColorGetsNothing() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attach(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Boost and keyword wear off when Clout is removed")
    void wearsOffWhenRemoved() {
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent clout = attach(wizard);

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(clout);

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Clout of the Dominus")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CloutOfTheDominus()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
