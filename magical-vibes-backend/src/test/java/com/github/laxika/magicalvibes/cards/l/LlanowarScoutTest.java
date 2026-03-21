package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlanowarScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Llanowar Scout has correct activated ability")
    void hasCorrectProperties() {
        LlanowarScout card = new LlanowarScout();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getTimingRestriction()).isNull();
        assertThat(ability.getEffects()).singleElement().isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) ability.getEffects().getFirst();
        assertThat(may.wrapped()).isInstanceOf(PutCardToBattlefieldEffect.class);
        PutCardToBattlefieldEffect wrapped = (PutCardToBattlefieldEffect) may.wrapped();
        assertThat(wrapped.cardType()).isEqualTo(CardType.LAND);
    }

    @Test
    @DisplayName("Activating ability taps Llanowar Scout and puts ability on stack")
    void activatingTapsAndUsesStack() {
        Permanent scout = addReadyScout(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(scout.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Llanowar Scout");
    }

    @Test
    @DisplayName("Resolving ability prompts may choice first")
    void resolvingPromptsMayChoice() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may prompt allows choosing only land cards from hand")
    void acceptingMayPromptsLandChoice() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.CARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Choosing a land puts it onto the battlefield untapped")
    void choosingLandPutsItOntoBattlefieldUntapped() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new Forest()));
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst()
                .orElseThrow();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ability can put a land onto battlefield even if a land was already played this turn")
    void canPutLandEvenAfterLandPlay() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new Forest()));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Declining may choice leaves hand unchanged")
    void decliningMayLeavesHandUnchanged() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Cannot activate while Llanowar Scout has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent scout = new Permanent(new LlanowarScout());
        gd.playerBattlefields.get(player1.getId()).add(scout);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate while Llanowar Scout is tapped")
    void cannotActivateWhileTapped() {
        Permanent scout = addReadyScout(player1);
        scout.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can activate during opponent's turn (instant speed)")
    void canActivateDuringOpponentsTurn() {
        addReadyScout(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate outside main phase (instant speed)")
    void canActivateOutsideMainPhase() {
        addReadyScout(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyScout(Player player) {
        Permanent scout = new Permanent(new LlanowarScout());
        scout.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(scout);
        return scout;
    }
}
