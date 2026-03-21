package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SparringConstruct;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThranTemporalGatewayTest extends BaseCardTest {

    @Test
    @DisplayName("Has correct activated ability structure")
    void hasCorrectProperties() {
        ThranTemporalGateway card = new ThranTemporalGateway();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{4}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).singleElement()
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(PutCardToBattlefieldEffect.class);
        PutCardToBattlefieldEffect wrapped = (PutCardToBattlefieldEffect) mayEffect.wrapped();
        assertThat(wrapped.predicate()).isNotNull();
        assertThat(wrapped.label()).isEqualTo("historic permanent");
    }

    @Test
    @DisplayName("Activating ability taps Gateway, spends mana, and goes on stack")
    void activatingAbilityUsesTapAndMana() {
        Permanent gateway = addReadyGateway();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gateway.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Thran Temporal Gateway");
    }

    @Test
    @DisplayName("Resolving ability prompts may choice first")
    void resolvingPromptsMayChoiceFirst() {
        addReadyGateway();
        harness.setHand(player1, List.of(new SparringConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may then resolving prompts card choice with only historic permanent indices")
    void resolvingPromptsOnlyHistoricPermanentChoices() {
        addReadyGateway();
        // SparringConstruct is an artifact creature (historic permanent) — index 0
        // GrizzlyBears is a regular creature (not historic) — index 1
        // LightningBolt is an instant (not a permanent) — index 2
        harness.setHand(player1, List.of(new SparringConstruct(), new GrizzlyBears(), new LightningBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.CARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a historic permanent puts it onto the battlefield")
    void choosingHistoricPermanentPutsItOntoBattlefield() {
        addReadyGateway();
        harness.setHand(player1, List.of(new SparringConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sparring Construct"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining may leaves hand unchanged")
    void decliningMayLeavesHandUnchanged() {
        addReadyGateway();
        harness.setHand(player1, List.of(new SparringConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        int battlefieldSizeBefore = harness.getGameData().playerBattlefields.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
    }

    @Test
    @DisplayName("No historic permanent cards in hand skips choice")
    void noHistoricPermanentsInHandSkipsChoice() {
        addReadyGateway();
        // GrizzlyBears is not historic, LightningBolt is not a permanent
        harness.setHand(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.CARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("has no historic permanent cards in hand"));
    }

    @Test
    @DisplayName("Non-creature artifact can activate tap ability the turn it enters")
    void canActivateWithoutSummoningSickness() {
        Permanent gateway = new Permanent(new ThranTemporalGateway());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(gateway);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gateway.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent gateway = addReadyGateway();
        gateway.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyGateway();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyGateway() {
        Permanent gateway = new Permanent(new ThranTemporalGateway());
        gateway.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(gateway);
        return gateway;
    }
}
