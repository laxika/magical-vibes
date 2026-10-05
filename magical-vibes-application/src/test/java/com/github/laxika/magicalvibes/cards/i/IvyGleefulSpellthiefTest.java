package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ColossalGrowth;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hammerhand;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.cards.s.ShoreUp;
import com.github.laxika.magicalvibes.cards.t.Twinferno;
import com.github.laxika.magicalvibes.cards.y.YavimayaIconoclast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IvyGleefulSpellthief.class, GiantGrowth.class, DualShot.class, GrizzlyBears.class,
        Hammerhand.class, ShoreUp.class, YavimayaIconoclast.class, Negate.class, Twinferno.class,
        ColossalGrowth.class, SeedsOfStrength.class})
class IvyGleefulSpellthiefTest extends BaseCardTest {

    @Test
    @DisplayName("Copies another player's single-creature-targeting spell onto Ivy")
    void copiesSpellTargetingAnotherCreature() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
        assertThat(copy.getTargetId()).isEqualTo(ivy.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.passBothPriorities();

        assertThat(ivy.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a spell with two creature targets")
    void doesNotCopyMultipleCreatureTargets() {
        harness.addToBattlefield(player1, new IvyGleefulSpellthief());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DualShot()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(firstBear.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when the spell targets Ivy")
    void doesNotCopySpellTargetingIvy() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, ivy.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ivy.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Copies an Aura onto Ivy as a token with its own ETB target")
    void copiesAuraWithIndependentEtbTarget() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Hammerhand()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castEnchantment(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getTargetId()).isEqualTo(ivy.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Hammerhand");
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getAttachedTo()).isEqualTo(ivy.getId());
                });
        assertThat(bear.isCantBlockThisTurn()).isTrue();
        assertThat(ivy.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Copies an opponent's spell that targets a creature they control")
    void copyUsesIvysControllerForTargetRestrictions() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        ivy.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YavimayaIconoclast());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ShoreUp()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).anySatisfy(copy -> {
            assertThat(copy.isCopy()).isTrue();
            assertThat(copy.getControllerId()).isEqualTo(player1.getId());
            assertThat(copy.getTargetId()).isEqualTo(ivy.getId());
        });
        harness.passBothPriorities();

        assertThat(ivy.getEffectivePower()).isEqualTo(3);
        assertThat(ivy.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, ivy, Keyword.HEXPROOF)).isTrue();
        assertThat(creature.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("May decline copying a spell without affecting the original")
    void mayDeclineCopy() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YavimayaIconoclast());
        harness.setHand(player1, List.of(new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        harness.passBothPriorities();

        assertThat(ivy.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Still copies a spell countered before the copying ability resolves")
    void copiesCounteredSpell() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YavimayaIconoclast());
        ShoreUp spell = new ShoreUp();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Shore Up");
        harness.passBothPriorities();
        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
        harness.passBothPriorities();

        assertThat(ivy.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Keeps the chosen mode of a copied modal spell")
    void keepsChosenMode() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YavimayaIconoclast());
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ivy, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A copied kicked spell keeps the original's kicker payment")
    void copiesKickedSpell() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YavimayaIconoclast());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ColossalGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castKickedInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ivy.getEffectivePower()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ivy, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ivy, Keyword.HASTE)).isTrue();
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(creature.getEffectivePower()).isEqualTo(7);
    }

    @Test
    @DisplayName("A spell with no targets does not trigger Ivy")
    void doesNotCopyUntargetedSpell() {
        harness.addToBattlefield(player1, new IvyGleefulSpellthief());
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copies a spell whose three target occurrences name the same creature")
    void copiesRepeatedTargetOccurrences() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YavimayaIconoclast());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SeedsOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, List.of(creature.getId(), creature.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ivy.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(creature.getEffectivePower()).isEqualTo(6);
    }
}
