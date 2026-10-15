package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladesOfVelisVel.class, GoblinKing.class, GrizzlyBears.class, Lignify.class, Mountain.class})
class BladesOfVelisVelTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Both target creatures get +2/+0")
    void twoTargetsGetBoost() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(a.getId(), b.getId()));

        assertThat(gqs.getEffectivePower(gd, a)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, a)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, b)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, b)).isEqualTo(2);
    }

    @Test
    @DisplayName("Targets gain all creature types, so Goblin King buffs a non-Goblin")
    void grantsAllCreatureTypes() {
        harness.addToBattlefield(player1, new GoblinKing());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2); // not a Goblin yet

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));

        // 2 base +2 (Blades) +1 (Goblin King, now a Goblin via Changeling)
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        // toughness 2 +0 (Blades) +1 (Goblin King)
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("May target only one creature (up to two)")
    void singleTargetAllowed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost and creature types wear off at end of turn")
    void wearsOff() {
        harness.addToBattlefield(player1, new GoblinKing());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2); // no boost, no longer a Goblin
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May resolve without choosing any targets")
    void zeroTargetsAllowed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Blades of Velis Vel");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Targets actually acquire all creature subtypes")
    void grantsEffectiveCreatureSubtypes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));

        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.MOUNTAIN)).isFalse();
    }

    @Test
    @DisplayName("Gaining creature types does not grant an ability to a Lignified creature")
    void gainsTypesWithoutGainingChangelingAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Lignify(), new BladesOfVelisVel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isFalse();
    }
}
