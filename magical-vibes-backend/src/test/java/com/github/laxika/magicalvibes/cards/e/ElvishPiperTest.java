package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvishPiperTest {

    private GameTestHarness harness;
    private Player player1;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Elvish Piper has correct card properties and activated ability")
    void hasCorrectProperties() {
        ElvishPiper card = new ElvishPiper();

        assertThat(card.getName()).isEqualTo("Elvish Piper");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ELF, CardSubtype.SHAMAN);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{G}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).singleElement()
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(PutCardToBattlefieldEffect.class);
        PutCardToBattlefieldEffect wrapped = (PutCardToBattlefieldEffect) mayEffect.wrapped();
        assertThat(wrapped.cardType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Activated ability taps Piper, spends mana, and goes on stack")
    void activatingAbilityUsesTapAndMana() {
        Permanent piper = addReadyPiper();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(piper.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Elvish Piper");
    }

    @Test
    @DisplayName("Resolving ability prompts may choice first")
    void resolvingPromptsMayChoiceFirst() {
        addReadyPiper();
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may then resolving prompts card choice with only creature indices")
    void resolvingPromptsOnlyCreatureChoices() {
        addReadyPiper();
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.CARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingCardChoiceValidIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Choosing a creature puts it onto the battlefield")
    void choosingCreaturePutsItOntoBattlefield() {
        addReadyPiper();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining may leaves hand unchanged")
    void decliningMayLeavesHandUnchanged() {
        addReadyPiper();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

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
    @DisplayName("Ability does not prompt when controller has no creature cards in hand")
    void noCreaturesInHandSkipsChoice() {
        addReadyPiper();
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.CARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("has no creature cards in hand"));
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent piper = new Permanent(new ElvishPiper());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(piper);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent piper = addReadyPiper();
        piper.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability without green mana")
    void cannotActivateWithoutMana() {
        addReadyPiper();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyPiper() {
        Permanent piper = new Permanent(new ElvishPiper());
        piper.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(piper);
        return piper;
    }
}
