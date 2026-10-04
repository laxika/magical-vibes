package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuriousStrength.class, GrizzlyBears.class, FountainOfYouth.class})
class FuriousStrengthTest extends BaseCardTest {

    @Test
    void canEnchantOpponentsCreatureWithoutAffectingOtherCreatures() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FuriousStrength()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Furious Strength").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.MENACE)).isFalse();
    }

    @Test
    void multipleCopiesStackBoostsAndRemainingCopyStillGrantsMenace() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FuriousStrength());
        first.setAttachedTo(bears.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FuriousStrength());
        second.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    void menaceRejectsOneBlockerButAllowsTwo() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FuriousStrength());
        aura.setAttachedTo(attacker.getId());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    void auraGoesToGraveyardWhenEnchantedCreatureLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FuriousStrength()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Furious Strength");
        harness.assertNotOnBattlefield(player1, "Furious Strength");
    }

    @Test
    void resolvingAttachesAndBoostsTarget() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FuriousStrength()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Furious Strength");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FuriousStrength());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FuriousStrength()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Furious Strength");
        harness.assertNotOnBattlefield(player1, "Furious Strength");
    }

    @Test
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FuriousStrength()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
