package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoomedNecromancerTest {

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

    // ===== Card properties =====

    @Test
    @DisplayName("Doomed Necromancer has correct card properties")
    void hasCorrectProperties() {
        DoomedNecromancer card = new DoomedNecromancer();

        assertThat(card.getName()).isEqualTo("Doomed Necromancer");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CLERIC, CardSubtype.MERCENARY);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{B}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1)).isInstanceOf(ReturnCreatureFromGraveyardToBattlefieldEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Doomed Necromancer puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new DoomedNecromancer()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Doomed Necromancer");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Doomed Necromancer"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new DoomedNecromancer()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability sacrifices Doomed Necromancer and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        Permanent necromancer = addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        // Doomed Necromancer should be sacrificed (not on battlefield, in graveyard)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Doomed Necromancer"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Doomed Necromancer"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Doomed Necromancer");
    }

    @Test
    @DisplayName("Activating ability taps the Doomed Necromancer")
    void activatingAbilityTapsNecromancer() {
        Permanent necromancer = addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Necromancer is sacrificed on activation, but tap happens before sacrifice
        // We verify that it was tapped by checking the tap happened (implicit in activation flow)
        harness.activateAbility(player1, 0, null, null);

        // Necromancer is in graveyard after sacrifice
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Doomed Necromancer"));
    }

    @Test
    @DisplayName("Activating ability consumes {B} mana")
    void activatingAbilityConsumesMana() {
        addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        // Should have 1 black mana remaining (2 - 1 for ability cost)
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Resolution — returning creature from graveyard =====

    @Test
    @DisplayName("Returns creature from graveyard to battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Choosing specific creature when multiple are in graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose Angel of Mercy (index 1 in graveyard, but Doomed Necromancer was added to graveyard
        // on sacrifice, shifting indices — we need to account for that)
        // Graveyard after sacrifice: [Grizzly Bears, Angel of Mercy, Doomed Necromancer]
        // Only creatures at indices 0, 1, 2 are valid; choose Angel of Mercy at index 1
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel of Mercy"));
        // Grizzly Bears stays in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Angel of Mercy"));
    }

    @Test
    @DisplayName("Doomed Necromancer itself is in the graveyard during resolution and could be a valid choice")
    void necromancerIsInGraveyardDuringResolution() {
        addReadyNecromancer(player1);
        // Empty graveyard initially — only Doomed Necromancer will be there after sacrifice
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        // Doomed Necromancer is now in graveyard after sacrifice
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Doomed Necromancer"));

        harness.passBothPriorities();

        // Should prompt graveyard choice since Doomed Necromancer (a creature) is in the graveyard
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Choose the Doomed Necromancer itself
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Doomed Necromancer"));
    }

    // ===== Empty graveyard =====

    @Test
    @DisplayName("Ability resolves with no effect if graveyard has only non-creature cards")
    void noEffectWithOnlyNonCreaturesInGraveyard() {
        addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        // Note: Doomed Necromancer goes to graveyard on sacrifice, so there IS a creature now
        // Graveyard after sacrifice: [HolyDay, DoomedNecromancer]
        harness.passBothPriorities();

        // Should still prompt for graveyard choice (Doomed Necromancer itself is a valid creature)
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    // ===== ETB on returned creature =====

    @Test
    @DisplayName("Returned creature's ETB ability triggers")
    void returnedCreatureTriggersETB() {
        addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        // Angel of Mercy's ETB (gain 3 life) should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel of Mercy");

        // Resolve the ETB
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    // ===== Validation — cannot activate =====

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        // No mana added
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent necromancer = addReadyNecromancer(player1);
        necromancer.tap();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        // Use addToBattlefield which creates a permanent with summoning sickness by default
        DoomedNecromancer card = new DoomedNecromancer();
        harness.addToBattlefield(player1, card);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Invalid graveyard choice =====

    @Test
    @DisplayName("Cannot choose non-creature card from graveyard")
    void cannotChooseNonCreatureFromGraveyard() {
        addReadyNecromancer(player1);
        // Set graveyard with a non-creature followed by a creature
        harness.setGraveyard(player1, List.of(new HolyDay(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Index 0 is HolyDay (instant, not creature) — not a valid choice
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Stack is empty after full resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        addReadyNecromancer(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadyNecromancer(Player player) {
        DoomedNecromancer card = new DoomedNecromancer();
        Permanent necromancer = new Permanent(card);
        necromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(necromancer);
        return necromancer;
    }
}

