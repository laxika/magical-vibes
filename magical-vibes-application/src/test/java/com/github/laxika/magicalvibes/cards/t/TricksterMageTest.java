package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TricksterMageTest extends BaseCardTest {

    @Test
    @DisplayName("{U}, {T}, Discard a card taps an untapped target creature")
    void tapsUntappedCreature() {
        addReadyMage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        activate(target);

        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("{U}, {T}, Discard a card untaps a tapped target land")
    void untapsTappedLand() {
        addReadyMage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();
        activate(target);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can target an artifact")
    void targetsArtifact() {
        addReadyMage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        activate(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyMage();
        harness.setHand(player1, new ArrayList<>());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyMage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    private void addReadyMage() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new TricksterMage());
        mage.setSummoningSick(false);
    }

    private void activate(Permanent target) {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
