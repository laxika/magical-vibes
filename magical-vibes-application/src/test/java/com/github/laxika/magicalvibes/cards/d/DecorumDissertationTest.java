package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecorumDissertationTest extends BaseCardTest {

    @Test
    @DisplayName("Has correct effects and targeting")
    void hasCorrectProperties() {
        DecorumDissertation card = new DecorumDissertation();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardForTargetPlayerEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(TargetPlayerLosesLifeEffect.class);
    }

    @Test
    @DisplayName("Target player draws two cards and loses 2 life")
    void resolvesAllEffectsOnOpponent() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castDecorumDissertationTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Is exiled after resolution and registers a Paradigm delayed trigger")
    void exilesAndRegistersParadigmTrigger() {
        castDecorumDissertationTargeting(player2.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Decorum Dissertation");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Decorum Dissertation"));
        assertThat(gd.paradigmDelayedTriggers).hasSize(1);
        assertThat(gd.paradigmResolvedSpellNames.get(player1.getId())).contains("Decorum Dissertation");
    }

    @Test
    @DisplayName("Paradigm trigger offers a free copy on a later precombat main phase")
    void paradigmTriggerOffersFreeCopyNextTurn() {
        castDecorumDissertationTargeting(player2.getId());
        harness.passBothPriorities();

        gd.turnNumber = 2;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        gs.advanceStep(gd);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);

        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).isEmpty();
    }

    @Test
    @DisplayName("Declining the Paradigm copy removes it from exile")
    void decliningParadigmCopyRemovesItFromExile() {
        castDecorumDissertationTargeting(player2.getId());
        harness.passBothPriorities();

        gd.turnNumber = 2;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        gs.advanceStep(gd);

        int exiledCopiesBefore = gd.getPlayerExiledCards(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(exiledCopiesBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new DecorumDissertation()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDecorumDissertationTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DecorumDissertation()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
