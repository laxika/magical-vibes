package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindOverMatterTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card lets it tap an untapped target creature")
    void discardingTapsUntappedCreature() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        activate(target);

        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Discarding a card lets it untap a tapped target land")
    void discardingUntapsTappedLand() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        activate(target);

        assertThat(target.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("It cannot target an enchantment")
    void cannotTargetEnchantment() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    @DisplayName("It cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMindOverMatter() {
        return harness.addToBattlefieldAndReturn(player1, new MindOverMatter());
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
