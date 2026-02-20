package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BounceOwnCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunkenHopeTest {

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

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private Permanent addCreature(Player player) {
        Card card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Sunken Hope has correct card properties")
    void hasCorrectProperties() {
        SunkenHope card = new SunkenHope();

        assertThat(card.getName()).isEqualTo("Sunken Hope");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst()).isInstanceOf(BounceOwnCreatureOnUpkeepEffect.class);
    }

    // ===== Triggering during controller's upkeep =====

    @Test
    @DisplayName("Triggers during controller's upkeep and prompts permanent choice")
    void triggersDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new SunkenHope());
        Permanent creature = addCreature(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).contains(creature.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Choosing a creature returns it to owner's hand")
    void choosingCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new SunkenHope());
        Permanent creature = addCreature(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    // ===== Triggering during opponent's upkeep =====

    @Test
    @DisplayName("Triggers during opponent's upkeep and prompts opponent to choose")
    void triggersDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SunkenHope());
        Permanent creature = addCreature(player2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).contains(creature.getId());
    }

    @Test
    @DisplayName("Opponent choosing a creature returns it to hand during their upkeep")
    void opponentBouncesOwnCreature() {
        harness.addToBattlefield(player1, new SunkenHope());
        Permanent creature = addCreature(player2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        harness.handlePermanentChosen(player2, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== No creatures =====

    @Test
    @DisplayName("Does nothing when active player controls no creatures")
    void doesNothingWithNoCreatures() {
        harness.addToBattlefield(player1, new SunkenHope());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // No choice prompted — enchantment is the only permanent and it's not a creature
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Does nothing for opponent's upkeep when opponent controls no creatures")
    void doesNothingWhenOpponentHasNoCreatures() {
        harness.addToBattlefield(player1, new SunkenHope());
        // player2 has no creatures

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Multiple creatures =====

    @Test
    @DisplayName("Only offers creatures as valid choices, not non-creature permanents")
    void onlyOffersCreatures() {
        harness.addToBattlefield(player1, new SunkenHope());
        Permanent creature = addCreature(player1);
        // Sunken Hope itself is on the battlefield but should not be offered

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Offers all creatures when player controls multiple")
    void offersAllCreatures() {
        harness.addToBattlefield(player1, new SunkenHope());
        Permanent creature1 = addCreature(player1);
        Permanent creature2 = addCreature(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).contains(creature1.getId(), creature2.getId());
    }

    @Test
    @DisplayName("Player can choose which creature to bounce when controlling multiple")
    void playerChoosesWhichCreatureToBounce() {
        harness.addToBattlefield(player1, new SunkenHope());
        Permanent creature1 = addCreature(player1);
        Permanent creature2 = addCreature(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Choose the second creature to bounce
        harness.handlePermanentChosen(player1, creature2.getId());

        // creature2 should be in hand, creature1 should remain on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature1.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature2.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Does not affect the other player =====

    @Test
    @DisplayName("Controller's upkeep does not force opponent to bounce")
    void controllersUpkeepDoesNotAffectOpponent() {
        harness.addToBattlefield(player1, new SunkenHope());
        Permanent myCreature = addCreature(player1);
        Permanent theirCreature = addCreature(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Should prompt player1, not player2
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
        // Opponent's creature should not be in valid choices
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).doesNotContain(theirCreature.getId());
    }
}


