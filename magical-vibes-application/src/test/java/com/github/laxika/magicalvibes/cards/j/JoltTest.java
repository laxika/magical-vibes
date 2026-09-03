package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.ManaPrism;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Jolt.class, BayFalcon.class, Forest.class, ManaPrism.class, Pacifism.class})
class JoltTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped creature and schedules a draw at the next upkeep")
    void tapsCreatureAndSchedulesDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        castJolt(target.getId());

        assertThat(target.isTapped()).isTrue();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps a tapped creature")
    void untapsTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        target.tap();

        castJolt(target.getId());

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a land")
    void tapsLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        castJolt(target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an artifact")
    void tapsArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaPrism());
        castJolt(target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player2, new BayFalcon());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        prepareJolt();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    @DisplayName("Does not draw immediately on resolution")
    void doesNotDrawImmediately() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castJolt(target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws a card at the beginning of the next turn's upkeep")
    void drawsAtNextUpkeep() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayFalcon());

        castJolt(target.getId());

        int handSizeBeforeDraw = gd.playerHands.get(player1.getId()).size();
        int deckSizeBeforeDraw = gd.playerDecks.get(player1.getId()).size();
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeDraw + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBeforeDraw - 1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("May decline to tap or untap the target")
    void mayDeclineTapOrUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        prepareJolt();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw if the target leaves before resolution")
    void doesNotResolveIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        prepareJolt();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    private void prepareJolt() {
        harness.setHand(player1, List.of(new Jolt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    private void castJolt(UUID targetId) {
        prepareJolt();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }
}
