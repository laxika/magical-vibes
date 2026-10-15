package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LeaveInTheDust;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.UniversalSolvent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TezzeretsTouch.class, IcyManipulator.class, DoomBlade.class, GrizzlyBears.class,
        Ornithopter.class, UniversalSolvent.class, LeaveInTheDust.class})
class TezzeretsTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted artifact becomes a 5/5 artifact creature")
    void enchantsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());

        castTezzeretsTouch(artifact);

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
    }

    @Test
    @DisplayName("When enchanted artifact is put into a graveyard, it returns to its owner's hand")
    void returnsEnchantedArtifactToOwnersHand() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        Card artifactCard = artifact.getCard();
        castTezzeretsTouch(artifact);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(artifactCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(artifactCard.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TezzeretsTouch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact creatures become base 5/5 and retain counters")
    void setsArtifactCreatureBasePowerAndToughness() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castTezzeretsTouch(artifact);

        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(7);
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
    }

    @Test
    @DisplayName("An opponent's enchanted artifact returns to that opponent's hand")
    void returnsOpponentsArtifactToItsOwner() {
        harness.addToBattlefield(player1, new UniversalSolvent());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castTezzeretsTouch(artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertNotInHand(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Tezzeret's Touch");

        harness.passBothPriorities();

        harness.assertInHand(player2, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Tezzeret's Touch");
    }

    @Test
    @DisplayName("Destroying the Aura ends animation without returning the artifact")
    void stopsAnimatingWhenAuraIsDestroyed() {
        harness.addToBattlefield(player1, new UniversalSolvent());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UniversalSolvent());
        castTezzeretsTouch(artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Tezzeret's Touch"));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        harness.assertOnBattlefield(player1, "Universal Solvent");
        harness.assertNotInHand(player1, "Universal Solvent");
        harness.assertInGraveyard(player1, "Tezzeret's Touch");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Animated artifacts retain their abilities and return after sacrifice")
    void returnsAfterSacrificeAsAnAbilityCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UniversalSolvent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        artifact.setSummoningSick(false);
        castTezzeretsTouch(artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Universal Solvent");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Universal Solvent");
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Tezzeret's Touch");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Universal Solvent");
    }

    @Test
    @DisplayName("Returning the artifact directly to hand does not trigger the Aura")
    void doesNotTriggerWhenArtifactIsBounced() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castTezzeretsTouch(artifact);
        harness.setHand(player1, List.of(new LeaveInTheDust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Tezzeret's Touch");
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("An animated noncreature artifact leaves combat when the Aura leaves")
    void removesArtifactFromCombatWhenAuraLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UniversalSolvent());
        artifact.setSummoningSick(false);
        castTezzeretsTouch(artifact);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        assertThat(artifact.isAttacking()).isTrue();

        harness.setHand(player1, List.of(new LeaveInTheDust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Tezzeret's Touch"));

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(artifact.isAttacking()).isFalse();
        harness.assertOnBattlefield(player1, "Universal Solvent");
        harness.assertInHand(player1, "Tezzeret's Touch");
    }
    private void castTezzeretsTouch(Permanent target) {
        harness.setHand(player1, List.of(new TezzeretsTouch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
