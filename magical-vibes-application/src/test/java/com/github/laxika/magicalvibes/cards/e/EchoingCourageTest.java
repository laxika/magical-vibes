package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EchoingCourage.class, DarksteelGargoyle.class, MyrMoonvessel.class})
class EchoingCourageTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target and all creatures with the same name +2/+2")
    void boostsTargetAndAllSameNameCreatures() {
        Permanent ownGargoyle = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent target = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent otherGargoyle = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent myr = addCreatureReady(player2, new MyrMoonvessel());

        castEchoingCourage(target.getId());

        assertThat(gqs.getEffectivePower(gd, ownGargoyle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownGargoyle)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherGargoyle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherGargoyle)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);
    }

    @Test
    @DisplayName("A same-name hexproof creature is affected without being targeted")
    void affectsSameNameHexproofCreature() {
        Permanent target = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent hexproof = addCreatureReady(player2, new DarksteelGargoyle());
        TestCards.mutableCard(hexproof).setKeywords(EnumSet.of(Keyword.HEXPROOF));

        castEchoingCourage(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, hexproof)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hexproof)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new DarksteelGargoyle());

        castEchoingCourage(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent otherGargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        harness.setHand(player1, List.of(new EchoingCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, otherGargoyle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherGargoyle)).isEqualTo(3);
    }

    @Test
    @DisplayName("Uses current creature names when finding affected creatures")
    @CardUsed(WitnessProtection.class)
    void usesEffectiveNamesWhenFindingSameNameCreatures() {
        Permanent target = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent renamed = addCreatureReady(player2, new DarksteelGargoyle());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new WitnessProtection());
        aura.setAttachedTo(renamed.getId());

        assertThat(gqs.getEffectiveName(gd, renamed)).isEqualTo("Legitimate Businessperson");

        castEchoingCourage(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, renamed)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, renamed)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new EchoingCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEchoingCourage(UUID targetId) {
        harness.setHand(player1, List.of(new EchoingCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
