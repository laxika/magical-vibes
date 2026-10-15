package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.u.UnquenchableThirst;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({MirageMirror.class, Forest.class, GrizzlyBears.class, GloriousAnthem.class,
        JaceBeleren.class, Manalith.class, FeralProwler.class, UnquenchableThirst.class, Unsummon.class})
class MirageMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a copy of target creature until end of turn")
    void becomesCopyOfCreature() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(mirror.getCard().getPower()).isEqualTo(2);
        assertThat(mirror.getCard().getToughness()).isEqualTo(2);
        assertThat(mirror.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("Becomes a copy of target land until end of turn")
    void becomesCopyOfLand() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Forest");
        assertThat(mirror.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Becomes a copy of target enchantment until end of turn")
    void becomesCopyOfEnchantment() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, anthem.getId());
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Glorious Anthem");
        assertThat(mirror.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
    }

    @Test
    @DisplayName("Becomes a copy of target artifact until end of turn")
    void becomesCopyOfArtifact() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent manalith = harness.addToBattlefieldAndReturn(player1, new Manalith());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, manalith.getId());
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Manalith");
        assertThat(mirror.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("Copy reverts at end of turn")
    void copyRevertsAtEndOfTurn() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(mirror.getCard().getName()).isEqualTo("Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Mirage Mirror");
        assertThat(mirror.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(mirror.getCard().hasType(CardType.CREATURE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a planeswalker")
    void cannotTargetPlaneswalker() {
        harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, jace.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pendingActivationsOverwriteCopiesAndRevertTogether() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.activateAbility(player1, 0, 0, null, prowler.getId());
        harness.passBothPriorities();
        assertThat(mirror.getCard().getName()).isEqualTo("Feral Prowler");
        harness.passBothPriorities();
        assertThat(mirror.getCard().getName()).isEqualTo("Forest");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(mirror.getCard().getName()).isEqualTo("Mirage Mirror");
    }

    @Test
    void copiedArtifactCanUseItsManaAbility() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent manalith = harness.addToBattlefieldAndReturn(player2, new Manalith());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, manalith.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(mirror.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(manalith.isTapped()).isFalse();
    }

    @Test
    void losesCopyAbilityWhileCopyingCreature() {
        harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, prowler.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, prowler.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetLeavingBeforeResolutionDoesNotChangeMirror() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, prowler.getId());
        harness.castAndResolveInstant(player2, 0, prowler.getId());
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Mirage Mirror");
        harness.assertInHand(player2, "Feral Prowler");
    }

    @Test
    void copyingAuraPutsPhysicalMirrorInGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new MirageMirror());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UnquenchableThirst());
        aura.setAttachedTo(prowler.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Unquenchable Thirst");
        harness.assertInGraveyard(player1, "Mirage Mirror");
        harness.assertOnBattlefield(player2, "Unquenchable Thirst");
        assertThat(prowler.isTapped()).isFalse();
    }
}
