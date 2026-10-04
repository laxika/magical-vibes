package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.ShatteringSpree;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GruulWarPlow.class, GruulNodorog.class, ShatteringSpree.class})
class GruulWarPlowTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have trample")
    void grantsTrampleToOwnCreatures() {
        Permanent plow = harness.addToBattlefieldAndReturn(player1, new GruulWarPlow());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GruulNodorog());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GruulNodorog());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, plow, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability animates Gruul War Plow into a 4/4 Juggernaut artifact creature")
    void animatesIntoCreature() {
        Permanent plow = harness.addToBattlefieldAndReturn(player1, new GruulWarPlow());
        addAnimationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, plow)).isTrue();
        assertThat(gqs.isArtifact(gd, plow)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plow)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, plow)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, plow, CardSubtype.JUGGERNAUT)).isTrue();
        assertThat(gqs.hasKeyword(gd, plow, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent plow = harness.addToBattlefieldAndReturn(player1, new GruulWarPlow());
        addAnimationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, plow)).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, plow)).isFalse();
        assertThat(gqs.isArtifact(gd, plow)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, plow, CardSubtype.JUGGERNAUT)).isFalse();
        assertThat(gqs.hasKeyword(gd, plow, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering later also receive trample, which ends when the Plow leaves")
    void trampleGrantUpdatesWithTheBattlefield() {
        Permanent plow = harness.addToBattlefieldAndReturn(player1, new GruulWarPlow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GruulNodorog());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.setHand(player1, List.of(new ShatteringSpree()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstantWithRepeatedCosts(player1, 0, plow.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gruul War Plow");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Animation uses the stack and can be activated while tapped")
    void tappedPlowAnimatesOnlyOnResolution() {
        Permanent plow = harness.addToBattlefieldAndReturn(player1, new GruulWarPlow());
        plow.tap();
        addAnimationMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.isCreature(gd, plow)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, plow)).isTrue();
        assertThat(plow.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, plow, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An animated Plow retains its ability and can animate again")
    void canActivateAgainWhileAnimated() {
        Permanent plow = harness.addToBattlefieldAndReturn(player1, new GruulWarPlow());
        addAnimationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addAnimationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, plow)).isTrue();
        assertThat(gqs.getEffectivePower(gd, plow)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, plow)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, plow, Keyword.TRAMPLE)).isTrue();
        assertThat(plow.isTapped()).isFalse();
    }

    private void addAnimationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
