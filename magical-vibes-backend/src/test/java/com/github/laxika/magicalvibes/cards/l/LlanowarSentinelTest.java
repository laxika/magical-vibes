package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAndSearchLibraryForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LlanowarSentinelTest {

    private GameTestHarness harness;
    private Player player1;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Llanowar Sentinel has correct card properties")
    void hasCorrectProperties() {
        LlanowarSentinel card = new LlanowarSentinel();

        assertThat(card.getName()).isEqualTo("Llanowar Sentinel");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ELF);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(PayManaAndSearchLibraryForCardNamedToBattlefieldEffect.class);
    }

    @Test
    @DisplayName("Resolving Llanowar Sentinel creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast(3);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Sentinel"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining may ability does not search library")
    void decliningMaySkipsSearch() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).noneMatch(entry -> entry.contains("searches their library"));
        assertThat(countSentinelsOnBattlefield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting may without enough mana does not search")
    void acceptingMayWithoutEnoughManaDoesNotSearch() {
        setupAndCast(3);
        setupLibraryWithSentinels();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(countSentinelsOnBattlefield()).isEqualTo(1);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("can't pay {1}{G}"));
    }

    @Test
    @DisplayName("Accepting may with enough mana allows searching for Llanowar Sentinel")
    void acceptingMayWithEnoughManaAllowsSearch() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchCards())
                .allMatch(c -> c.getName().equals("Llanowar Sentinel"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Choosing Llanowar Sentinel from search puts it onto battlefield")
    void choosingSentinelPutsItOntoBattlefield() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(countSentinelsOnBattlefield()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private void setupAndCast(int greenMana) {
        harness.setHand(player1, List.of(new LlanowarSentinel()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithSentinels() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(
                new GrizzlyBears(),
                new LlanowarSentinel(),
                new LlanowarSentinel(),
                new GrizzlyBears()
        ));
    }

    private long countSentinelsOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Sentinel"))
                .count();
    }
}
