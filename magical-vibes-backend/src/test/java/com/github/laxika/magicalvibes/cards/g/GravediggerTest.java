package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GravediggerTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    /**
     * Casts Gravedigger and resolves it onto the battlefield, then accepts the may ability
     * so the ETB triggered ability is placed on the stack.
     */
    private void castAndAcceptMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → ETB on stack
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Gravedigger has correct card properties")
    void hasCorrectProperties() {
        Gravedigger card = new Gravedigger();

        assertThat(card.getName()).isEqualTo("Gravedigger");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ReturnCreatureFromGraveyardToHandEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Gravedigger puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gravedigger");
    }

    @Test
    @DisplayName("Cannot cast Gravedigger without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts Gravedigger on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gravedigger"));
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving Gravedigger triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve → may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may ability puts ETB triggered ability on stack")
    void acceptingMayPutsEtbOnStack() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gravedigger");
    }

    @Test
    @DisplayName("Declining may ability does not put anything on the stack")
    void decliningMaySkipsAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        // Gravedigger still on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gravedigger"));
        // Grizzly Bears still in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Graveyard return to hand =====

    @Test
    @DisplayName("Returns creature from graveyard to hand")
    void returnsCreatureFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        // Resolve ETB trigger → graveyard choice prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Choose the creature (index 0)
        harness.handleGraveyardCardChosen(player1, 0);

        // Grizzly Bears moved from graveyard to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Player can decline graveyard choice")
    void playerCanDeclineGraveyardChoice() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Decline with -1
        harness.handleGraveyardCardChosen(player1, -1);

        // Grizzly Bears stays in graveyard, not in hand
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Choosing specific creature when multiple are in graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new AngelOfMercy()));
        castAndAcceptMay();

        harness.passBothPriorities();

        // Choose Angel of Mercy (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        // Angel of Mercy returned to hand, Grizzly Bears stays in graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Angel of Mercy"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Angel of Mercy"));
    }

    // ===== Empty / no creatures in graveyard =====

    @Test
    @DisplayName("ETB resolves with no effect if graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        // No graveyard set — empty by default
        castAndAcceptMay();

        // Resolve ETB trigger — should resolve without graveyard choice
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no creature cards in graveyard"));
    }

    @Test
    @DisplayName("ETB resolves with no effect if graveyard has only non-creature cards")
    void noEffectWithOnlyNonCreaturesInGraveyard() {
        harness.setGraveyard(player1, List.of(new HolyDay()));
        castAndAcceptMay();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no creature cards in graveyard"));
        // Holy Day stays in graveyard untouched
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Holy Day"));
    }

    // ===== Invalid choices =====

    @Test
    @DisplayName("Cannot choose non-creature card from graveyard")
    void cannotChooseNonCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new HolyDay(), new GrizzlyBears()));
        castAndAcceptMay();

        harness.passBothPriorities();

        // Index 0 is HolyDay (instant, not creature) — not a valid choice
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Stack is empty after full resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gravedigger remains on battlefield after returning a creature")
    void gravediggerRemainsOnBattlefield() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gravedigger"));
    }
}


