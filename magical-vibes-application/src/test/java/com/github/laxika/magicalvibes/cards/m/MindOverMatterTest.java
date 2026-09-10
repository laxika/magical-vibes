package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.m.MemoryCrystal;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindOverMatter.class, RagingGoblin.class, CityOfTraitors.class, MemoryCrystal.class})
class MindOverMatterTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card lets it tap an untapped target creature")
    void discardingTapsUntappedCreature() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new CityOfTraitors()));

        activate(target);

        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "City of Traitors");
    }

    @Test
    @DisplayName("Discarding a card lets it untap a tapped target land")
    void discardingUntapsTappedLand() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        target.tap();
        harness.setHand(player1, List.of(new RagingGoblin()));

        activate(target);

        assertThat(target.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Discarding a card lets it tap an untapped target artifact")
    void discardingTapsUntappedArtifact() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MemoryCrystal());
        harness.setHand(player1, List.of(new RagingGoblin()));

        activate(target);

        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("It may decline to tap or untap the target")
    void mayDeclineToTapOrUntap() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new CityOfTraitors()));

        activate(target, false);

        assertThat(target.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "City of Traitors");
    }

    @Test
    @DisplayName("It cannot target an enchantment")
    void cannotTargetEnchantment() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindOverMatter());
        harness.setHand(player1, List.of(new RagingGoblin()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    @DisplayName("It cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addMindOverMatter();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMindOverMatter() {
        return harness.addToBattlefieldAndReturn(player1, new MindOverMatter());
    }

    private void activate(Permanent target) {
        activate(target, true);
    }

    private void activate(Permanent target, boolean accept) {
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
    }
}
