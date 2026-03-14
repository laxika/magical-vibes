package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAndSearchLibraryForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LlanowarSentinelTest extends BaseCardTest {


    @Test
    @DisplayName("Llanowar Sentinel has correct card properties")
    void hasCorrectProperties() {
        LlanowarSentinel card = new LlanowarSentinel();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(PayManaAndSearchLibraryForCardNamedToBattlefieldEffect.class);
    }

    @Test
    @DisplayName("Resolving Llanowar Sentinel creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast(3);

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Sentinel"));

        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining may ability does not search library")
    void decliningMaySkipsSearch() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
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

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline (can't pay)

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(countSentinelsOnBattlefield()).isEqualTo(1);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("can't pay {1}{G}"));
    }

    @Test
    @DisplayName("Accepting may with enough mana allows searching for Llanowar Sentinel")
    void acceptingMayWithEnoughManaAllowsSearch() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline (pays mana, shows search)

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.getName().equals("Llanowar Sentinel"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Choosing Llanowar Sentinel from search puts it onto battlefield")
    void choosingSentinelPutsItOntoBattlefield() {
        setupAndCast(5);
        setupLibraryWithSentinels();

        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline (pays mana, shows search)

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
