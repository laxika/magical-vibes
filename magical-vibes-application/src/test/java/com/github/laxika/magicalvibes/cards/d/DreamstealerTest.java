package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreamstealerTest extends BaseCardTest {

    // ===== Combat-damage discard trigger =====

    @Test
    @DisplayName("Combat damage to a player makes that player discard cards equal to the damage dealt")
    void discardsCardsEqualToCombatDamage() {
        // Two +1/+1 counters make the 1/2 Dreamstealer deal 3 combat damage.
        Permanent dreamstealer = addAttackingDreamstealer(player1);
        dreamstealer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new Forest())));

        resolveCombat();

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
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        resolveCombat();

        // No combat damage reached the player, so no discard was prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    // ===== Eternalize =====

    @Test
    @DisplayName("Eternalize exiles the source card from the graveyard and makes a 4/4 black Zombie token copy")
    void eternalizeCreatesFourFourBlackZombieToken() {
        setUpEternalize();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Eternalize ability

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Dreamstealer"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dreamstealer"));

        Permanent token = eternalizedToken();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.HUMAN, CardSubtype.WIZARD);
        assertThat(token.getCard().getManaCost()).isEmpty();
        // The token still carries the combat-damage discard trigger via Menace.
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dreamstealer"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttackingDreamstealer(Player player) {
        Permanent dreamstealer = addReadyCreature(player, new Dreamstealer());
        dreamstealer.setAttacking(true);
        return dreamstealer;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        // Pass through combat damage and the resulting triggered ability resolution.
        harness.passBothPriorities();
        harness.passBothPriorities();
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
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dreamstealer") && p.getCard().isToken())
                .findFirst().orElseThrow();
    }
}
