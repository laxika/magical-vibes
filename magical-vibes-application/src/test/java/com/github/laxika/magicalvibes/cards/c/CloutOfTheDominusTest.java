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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloutOfTheDominusTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent clout = new Permanent(new CloutOfTheDominus());
        clout.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(clout);
        return clout;
    }

    // ===== Blue: +1/+1 and shroud =====

    @Test
    @DisplayName("Blue enchanted creature gets +1/+1 and shroud")
    void blueGetsBoostAndShroud() {
        Permanent wizard = new Permanent(new FugitiveWizard()); // 1/1 blue
        wizard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wizard);
        attach(wizard);

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.HASTE)).isFalse();
    }

    // ===== Red: +1/+1 and haste =====

    @Test
    @DisplayName("Red enchanted creature gets +1/+1 and haste")
    void redGetsBoostAndHaste() {
        Permanent giant = new Permanent(new HillGiant()); // 3/3 red
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(giant);
        attach(giant);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Haste from Clout lets a summoning-sick red creature attack")
    void hasteAllowsSummoningSickAttack() {
        Permanent giant = new Permanent(new HillGiant()); // 3/3 red, summoning sick
        giant.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(giant);
        attach(giant);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Giant is battlefield index 0 (Clout aura added after). Succeeding proves haste.
        gs.declareAttackers(gd, player1, List.of(0));
    }

    // ===== Off-color: nothing =====

    @Test
    @DisplayName("Non-blue non-red enchanted creature gets no boost or keywords")
    void offColorGetsNothing() {
        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2 green
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        attach(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Boost and keyword wear off when Clout is removed")
    void wearsOffWhenRemoved() {
        Permanent wizard = new Permanent(new FugitiveWizard()); // 1/1 blue
        wizard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wizard);
        Permanent clout = attach(wizard);

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(clout);

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.SHROUD)).isFalse();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Clout of the Dominus")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CloutOfTheDominus()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
