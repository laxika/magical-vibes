package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
