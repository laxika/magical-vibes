package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreamstealer.class, FeralProwler.class, Forest.class})
class DreamstealerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes that player discard cards equal to the damage dealt")
    void discardsCardsEqualToCombatDamage() {
        // Two +1/+1 counters make the 1/2 Dreamstealer deal 3 combat damage.
        Permanent dreamstealer = addAttackingDreamstealer(player1);
        dreamstealer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, new ArrayList<>(List.of(new FeralProwler(), new Forest(), new Forest())));

        resolveCombatAndTrigger();

        // Damaged player must discard exactly three cards, one choice at a time.
        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                    .isEqualTo(player2.getId());
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("No trigger when Dreamstealer is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingDreamstealer(player1);
        for (int i = 0; i < 2; i++) {
            Permanent blocker = addCreatureReady(player2, new FeralProwler());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
        }
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        resolveCombatAndTrigger();

        // No combat damage reached the player, so no discard was prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Eternalize exiles the source card from the graveyard and makes a 4/4 black Zombie token copy")
    void eternalizeCreatesFourFourBlackZombieToken() {
        setUpEternalize();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Eternalize ability

        harness.assertNotInGraveyard(player1, "Dreamstealer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dreamstealer"));

        Permanent token = eternalizedToken();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.HUMAN, CardSubtype.WIZARD);
        assertThat(token.getCard().getManaCost()).isEmpty();
        // The token retains menace in addition to its combat-damage discard trigger.
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Eternalize can only be activated at sorcery speed")
    void eternalizeOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new Dreamstealer()));
        addEternalizeMana();

        // Opponent's turn — not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Dreamstealer");
    }

    @Test
    @DisplayName("The eternalized token makes the damaged player discard four cards")
    void eternalizedTokenDiscardsFourCards() {
        setUpEternalize();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent token = eternalizedToken();
        token.setSummoningSick(false);
        token.setAttacking(true);
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        resolveCombatAndTrigger();

        for (int i = 0; i < 4; i++) {
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                    .isEqualTo(player2.getId());
            harness.handleCardChosen(player2, 0);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("A player with fewer cards than the damage discards their entire hand")
    void discardsAvailableCardsWhenHandIsTooSmall() {
        Permanent dreamstealer = addAttackingDreamstealer(player1);
        dreamstealer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The discard amount remains the damage dealt when power changes before resolution")
    void discardAmountDoesNotUsePowerAtResolution() {
        Permanent dreamstealer = addAttackingDreamstealer(player1);
        dreamstealer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        resolveCombat();
        dreamstealer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player2, 0);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Combat damage to a player with an empty hand completes without a discard choice")
    void emptyHandDoesNotRequireInput() {
        addAttackingDreamstealer(player1);
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingDreamstealer(Player player) {
        Permanent dreamstealer = addCreatureReady(player, new Dreamstealer());
        dreamstealer.setAttacking(true);
        return dreamstealer;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }

    private void addEternalizeMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void setUpEternalize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Dreamstealer()));
        addEternalizeMana();
    }

    private Permanent eternalizedToken() {
        return findPermanent(player1, "Dreamstealer");
    }
}
