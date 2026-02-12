package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReyaDawnbringerTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    /**
     * Advances from UNTAP to UPKEEP, triggering upkeep abilities.
     * After this call, Reya's triggered ability is on the stack.
     */
    private void advanceToUpkeepAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP → triggers fire
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Reya Dawnbringer has correct card properties")
    void hasCorrectProperties() {
        ReyaDawnbringer card = new ReyaDawnbringer();

        assertThat(card.getName()).isEqualTo("Reya Dawnbringer");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{6}{W}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(4);
        assertThat(card.getToughness()).isEqualTo(6);
        assertThat(card.getKeywords()).contains(Keyword.FLYING);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ReturnCreatureFromGraveyardToBattlefieldEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Reya Dawnbringer puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ReyaDawnbringer()));
        harness.addMana(player1, "W", 9);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reya Dawnbringer");
    }

    @Test
    @DisplayName("Cannot cast Reya Dawnbringer without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new ReyaDawnbringer()));
        harness.addMana(player1, "W", 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts Reya Dawnbringer on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ReyaDawnbringer()));
        harness.addMana(player1, "W", 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Reya Dawnbringer"));
    }

    // ===== Upkeep trigger =====

    @Test
    @DisplayName("Upkeep trigger puts ability on the stack")
    void upkeepTriggerPutsAbilityOnStack() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeepAndTrigger();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reya Dawnbringer");
    }

    @Test
    @DisplayName("Upkeep trigger fires even with summoning sickness")
    void triggerFiresWithSummoningSickness() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        // summoning sick by default
        gd.playerBattlefields.get(player1.getId()).add(reya);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeepAndTrigger();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reya Dawnbringer");
    }

    @Test
    @DisplayName("No trigger during opponent's upkeep")
    void noTriggerDuringOpponentsUpkeep() {
        // Reya on player1's battlefield, but it's player2's turn
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP — only checks player2's permanents

        // No trigger from player1's Reya
        assertThat(gd.gameLog).noneMatch(s -> s.contains("Reya Dawnbringer"));
    }

    // ===== Graveyard return =====

    @Test
    @DisplayName("Returns creature from graveyard to battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeepAndTrigger();

        // Resolve trigger → graveyard choice prompt
        harness.passBothPriorities();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Choose the creature (index 0)
        harness.handleGraveyardCardChosen(player1, 0);

        // Grizzly Bears moved from graveyard to battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Player can decline graveyard choice")
    void playerCanDeclineGraveyardChoice() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeepAndTrigger();
        harness.passBothPriorities();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Decline with -1
        harness.handleGraveyardCardChosen(player1, -1);

        // Grizzly Bears stays in graveyard, not on battlefield
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Choosing specific creature when multiple are in graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new AngelOfMercy()));

        advanceToUpkeepAndTrigger();
        harness.passBothPriorities();

        // Choose Angel of Mercy (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        // Angel of Mercy returned, Grizzly Bears stays
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel of Mercy"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Angel of Mercy"));
    }

    // ===== No creatures in graveyard =====

    @Test
    @DisplayName("Trigger resolves with no effect if graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        // Empty graveyard — no cards at all

        advanceToUpkeepAndTrigger();
        assertThat(gd.stack).hasSize(1);

        // Resolve trigger — should resolve without graveyard choice
        harness.passBothPriorities();

        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no creature cards in graveyard"));
    }

    @Test
    @DisplayName("Trigger resolves with no effect if graveyard has only non-creature cards")
    void noEffectWithOnlyNonCreaturesInGraveyard() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        // Only instants in graveyard
        harness.setGraveyard(player1, List.of(new HolyDay()));

        advanceToUpkeepAndTrigger();
        harness.passBothPriorities();

        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no creature cards in graveyard"));
        // HolyDay stays in graveyard untouched
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Holy Day"));
    }

    // ===== ETB on returned creature =====

    @Test
    @DisplayName("Returned creature's ETB ability triggers")
    void returnedCreatureTriggersETB() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.setLife(player1, 20);

        advanceToUpkeepAndTrigger();
        harness.passBothPriorities(); // resolve Reya's trigger → graveyard choice
        harness.handleGraveyardCardChosen(player1, 0); // return Angel of Mercy

        // Angel of Mercy's ETB (gain 3 life) should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel of Mercy");

        // Resolve the ETB
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    // ===== Invalid choices =====

    @Test
    @DisplayName("Cannot choose invalid graveyard index")
    void cannotChooseInvalidIndex() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        // Graveyard: index 0 = HolyDay (instant), index 1 = GrizzlyBears (creature)
        harness.setGraveyard(player1, List.of(new HolyDay(), new GrizzlyBears()));

        advanceToUpkeepAndTrigger();
        harness.passBothPriorities();

        // Index 0 is HolyDay (not a creature) → not in valid indices
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        Permanent reya = new Permanent(new ReyaDawnbringer());
        reya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(reya);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeepAndTrigger();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }
}
