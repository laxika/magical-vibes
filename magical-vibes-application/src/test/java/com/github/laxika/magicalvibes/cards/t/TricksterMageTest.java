package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BelbesPortal;
import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.o.Oraxid;
import com.github.laxika.magicalvibes.cards.s.SpiritualAsylum;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TricksterMage.class, Oraxid.class, KorHaven.class, BelbesPortal.class, SpiritualAsylum.class})
class TricksterMageTest extends BaseCardTest {

    @Test
    @DisplayName("{U}, {T}, Discard a card taps an untapped target creature")
    void tapsUntappedCreature() {
        addCreatureReady(player1, new TricksterMage());
        Permanent target = addCreatureReady(player2, new Oraxid());
        activate(target);

        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Oraxid");
    }

    @Test
    @DisplayName("{U}, {T}, Discard a card untaps a tapped target land")
    void untapsTappedLand() {
        addCreatureReady(player1, new TricksterMage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KorHaven());
        target.tap();
        activate(target);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can target an artifact")
    void targetsArtifact() {
        addCreatureReady(player1, new TricksterMage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BelbesPortal());
        activate(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new TricksterMage());
        harness.setHand(player1, List.of());
        Permanent target = addCreatureReady(player2, new Oraxid());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addCreatureReady(player1, new TricksterMage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpiritualAsylum());
        harness.setHand(player1, List.of(new Oraxid()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    @DisplayName("Activating the ability taps Trickster Mage as part of its cost")
    void activationTapsMage() {
        Permanent mage = addCreatureReady(player1, new TricksterMage());
        Permanent target = addCreatureReady(player2, new Oraxid());

        activateAndPayDiscard(target);

        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the tap ability while Trickster Mage has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new TricksterMage());
        Permanent target = addCreatureReady(player2, new Oraxid());
        harness.setHand(player1, List.of(new Oraxid()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller may decline to tap or untap the target")
    void mayDeclineToTapOrUntap() {
        addCreatureReady(player1, new TricksterMage());
        Permanent target = addCreatureReady(player2, new Oraxid());

        activateAndPayDiscard(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Oraxid");
    }

    @Test
    @DisplayName("The ability fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        addCreatureReady(player1, new TricksterMage());
        Permanent target = addCreatureReady(player2, new Oraxid());

        activateAndPayDiscard(target);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    private void activate(Permanent target) {
        activateAndPayDiscard(target);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
    }

    private void activateAndPayDiscard(Permanent target) {
        harness.setHand(player1, List.of(new Oraxid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
    }
}
